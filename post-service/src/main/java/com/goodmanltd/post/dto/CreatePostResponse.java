package com.goodmanltd.post.dto;

import com.goodmanltd.core.types.BookReference;
import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.OrderReference;
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
	private MemberReference postBy;
	private BookReference bookRef;

	private String location;
	private String remarks;
	private LocalDateTime createdAt;

	private PostStatus postStatus;
}


