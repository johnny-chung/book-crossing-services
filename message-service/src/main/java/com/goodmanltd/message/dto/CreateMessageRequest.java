package com.goodmanltd.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequest {

	@NotNull
	private UUID senderId;

	@NotNull
	private UUID receiverId;

	@NotNull
	private UUID postId;

	@NotNull
	private UUID participantId;

	@NotNull
	@NotBlank
	private String content;
}
