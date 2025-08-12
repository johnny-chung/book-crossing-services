package com.goodmanltd.post.dto;

import com.goodmanltd.core.types.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostResponse {
	private UUID id;
	private UUID postBy;
	private UUID bookId;
	private String location;
	private String remarks;
	private LocalDateTime createdAt;
	private PostStatus postStatus;
}


