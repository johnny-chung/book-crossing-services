package com.goodmanltd.message.web.controller;

import com.goodmanltd.core.types.Message;
import com.goodmanltd.message.dto.*;
import com.goodmanltd.message.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

	@GetMapping("/conversation-list/{postId}")
	@PreAuthorize("isAuthenticated()")
	public GetParticipantListByPost getParticipantListByPost(
			@PathVariable UUID postId,
			@AuthenticationPrincipal Jwt jwt
	) {
		String auth0Id = jwt.getClaimAsString("sub");
		return messageService.getParticipantListByPost(postId, auth0Id);
	}

	@GetMapping("/member/conversation-list")
	@PreAuthorize("isAuthenticated()")
	public List<GetParticipantListByPost> getParticipantListByAuth0Id(
			@AuthenticationPrincipal Jwt jwt
	) {
		String auth0Id = jwt.getClaimAsString("sub");
		return messageService.getParticipantListByAuth0Id(auth0Id);
	}

	@GetMapping("/health")
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("Message Service is healthy");
	}
}
