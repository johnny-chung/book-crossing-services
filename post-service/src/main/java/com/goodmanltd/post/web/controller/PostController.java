package com.goodmanltd.post.web.controller;


import com.goodmanltd.core.types.Post;
import com.goodmanltd.post.dto.CreatePostRequest;
import com.goodmanltd.post.dto.CreatePostResponse;
import com.goodmanltd.post.dto.GetPostDetailsResponse;
import com.goodmanltd.post.service.PostService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

	private final PostService postService;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public List<Post> getPosts(
			@RequestParam(required = false) List<String> category,
			@RequestParam(required = false) List<String> language,
			@RequestParam(defaultValue = "desc") String order,
			@RequestParam(required = false) String search
	) {
		Sort sort = order.equalsIgnoreCase("asc")
				? Sort.by(Sort.Direction.ASC, "createdAt")
				: Sort.by(Sort.Direction.DESC, "createdAt");

		return postService.searchPosts(category, language, search, sort);
	}


	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CreatePostResponse createPost(
			@RequestBody @Valid CreatePostRequest request)
	{
		Post createdPost = postService.createPost(request);

		var response = new CreatePostResponse();
		BeanUtils.copyProperties(createdPost, response);
		return response;
	}

	@GetMapping("/{postId}")
	public GetPostDetailsResponse getPostDetails(@PathVariable UUID postId) {
		return postService.findByPostId(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
	}

	@GetMapping("/my-posts")
	@PreAuthorize("isAuthenticated()")
	public List<Post> getPostDetails(@AuthenticationPrincipal Jwt jwt) {
		String auth0Id = jwt.getClaimAsString("sub");
		return postService.findMemberPost(auth0Id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
	}

	@GetMapping("/available")
	public List<Post> getAvailablePosts() {
		return postService.findByAvailable()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Postd not found"));
	}


	@GetMapping("/all")
	public List<Post> getAll(){
		return postService.findAll()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Posts not found"));
	}

	@GetMapping("/health")
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("Post Service is healthy");
	}
}
