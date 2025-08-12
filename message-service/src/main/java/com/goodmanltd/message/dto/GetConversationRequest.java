package com.goodmanltd.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetConversationRequest {
	@NotNull
	@NotBlank
	private UUID postId;

	@NotNull
	@NotBlank
	private UUID participantId;

	private Number limit;

	private Number page;

	private UUID nextMsgId;
}
