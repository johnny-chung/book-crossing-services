package com.goodmanltd.message.service;

import com.goodmanltd.core.types.Message;
import com.goodmanltd.message.dto.CreateMessageRequest;
import com.goodmanltd.message.dto.GetConversationRequest;
import com.goodmanltd.message.dto.GetConversationResponse;
import com.goodmanltd.message.dto.GetParticipantListByPost;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface MessageService {
	Message createMessage (CreateMessageRequest request, String auth0Id);

	GetConversationResponse getConversation(GetConversationRequest request);

	GetParticipantListByPost getParticipantListByPost(UUID postId, String requestAuth0Id);

	List<GetParticipantListByPost> getParticipantListByAuth0Id(String requestAuth0Id);

	Optional<List<Message>> getAll() ;
}
