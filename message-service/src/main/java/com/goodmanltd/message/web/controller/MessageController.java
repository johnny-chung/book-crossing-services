package com.goodmanltd.message.web.controller;

import com.goodmanltd.core.types.Message;
import com.goodmanltd.message.dto.CreateMessageRequest;
import com.goodmanltd.message.dto.CreateMessageResponse;
import com.goodmanltd.message.dto.GetConversationRequest;
import com.goodmanltd.message.dto.GetConversationResponse;
import com.goodmanltd.message.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class MessageController {
	private final MessageService messageService;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public MessageController( MessageService messageService) {
		this.messageService = messageService;
	}


	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CreateMessageResponse createMessage(
			@RequestBody @Valid CreateMessageRequest request)
	{
		Message createdMessage = messageService.createMessage(request);

		var response = new CreateMessageResponse();
		BeanUtils.copyProperties(createdMessage, response);
		return response;
	}

	@GetMapping("/conversation/{postId}")
	public GetConversationResponse getConversation(
			@PathVariable UUID postId,
			@RequestParam UUID participantId,
			@RequestParam Number page,
			@RequestParam UUID nextMsgId
	) {
		GetConversationRequest request =
				new GetConversationRequest(postId, participantId, 25, page, nextMsgId);
		return messageService.getConversation(request);

	}
}
