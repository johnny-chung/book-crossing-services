package com.goodmanltd.message.service;

import com.goodmanltd.core.types.Message;
import com.goodmanltd.message.dto.CreateMessageRequest;
import com.goodmanltd.message.dto.GetConversationRequest;
import com.goodmanltd.message.dto.GetConversationResponse;

import java.util.List;
import java.util.Optional;


public interface MessageService {
	Message createMessage (CreateMessageRequest request);

	GetConversationResponse getConversation(GetConversationRequest request);

	Optional<List<Message>> getAll() ;
}
