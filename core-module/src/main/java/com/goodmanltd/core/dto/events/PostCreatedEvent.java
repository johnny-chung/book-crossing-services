package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.BookReference;
import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.PostStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedEvent {
	private UUID id;
	private MemberReference postBy;
	private BookReference bookRef;
	private String location;
	private String remarks;
	private LocalDateTime createdAt;
	private PostStatus postStatus;
}
