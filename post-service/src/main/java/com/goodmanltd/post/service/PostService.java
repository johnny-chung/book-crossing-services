package com.goodmanltd.post.service;

import com.goodmanltd.core.types.Post;
import com.goodmanltd.post.dto.CreatePostRequest;
import com.goodmanltd.post.dto.GetPostDetailsResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PostService {
	Post createPost(CreatePostRequest request);

	Optional<GetPostDetailsResponse> findByPostId(UUID postId);

	Optional<List<Post>> findByOrderId(UUID orderId);
	Optional<List<Post>> findMemberPost(String auth0Id);

	Optional<List<Post>> findByAvailable();

	Optional<List<Post>> findAll();
}
