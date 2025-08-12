package com.goodmanltd.core.dto.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {
	private UUID id;
	private UUID senderId;
	private UUID receiverId;
	private UUID postId;
	private UUID participantId;
	private LocalDateTime sentAt;
	private String content;
}
