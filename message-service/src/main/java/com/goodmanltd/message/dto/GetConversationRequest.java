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
	private UUID postId;

	@NotNull
	private UUID participantId;

	private Number page;

	private Integer limit;

	private UUID startMsgId;

	private UUID nextMsgId;
}
