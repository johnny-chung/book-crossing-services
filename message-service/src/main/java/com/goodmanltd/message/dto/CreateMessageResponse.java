package com.goodmanltd.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageResponse {
	private UUID id;
	private UUID senderId;
	private UUID receiverId;
	private UUID postId;
	private UUID participantId;
	private String participantName;
	private LocalDateTime sentAt;
	private String content;
}
