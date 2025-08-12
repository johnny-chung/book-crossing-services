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
	@NotBlank
	private UUID senderId;

	@NotNull
	@NotBlank
	private UUID receiverId;

	@NotNull
	@NotBlank
	private UUID postId;

	@NotNull
	@NotBlank
	private UUID participantId;

	@NotNull
	@NotBlank
	private String content;
}
