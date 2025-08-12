package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.PostStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedEvent {
	private UUID postId;
	private UUID postBy;
	private UUID bookId;
	private String bookTitle;
	private String thumbnail;
	private String location;
	private String remarks;
	private LocalDateTime createdAt;
	private PostStatus postStatus;
}
