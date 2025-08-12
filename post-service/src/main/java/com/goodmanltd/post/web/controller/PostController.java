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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

	@GetMapping("/all")
	public List<Post> getAll(){
		return postService.findAll()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Posts not found"));
	}
}
