package com.goodmanltd.message.service;

import com.goodmanltd.core.types.Message;
import com.goodmanltd.core.dto.events.MessageCreatedEvent;
import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.exceptions.MemberNotVerifiedException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.message.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.message.dao.mongo.entity.MessageMongoEntity;
import com.goodmanltd.message.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.message.dao.mongo.entity.mapper.MessageMongoMapper;
import com.goodmanltd.message.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.message.dao.mongo.repository.MessageMongoRepository;
import com.goodmanltd.message.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.message.dto.CreateMessageRequest;
import com.goodmanltd.message.dto.GetConversationRequest;
import com.goodmanltd.message.dto.GetConversationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Service
public class MessageServiceMongoImpl implements MessageService{

	private final MemberMongoRepository memberRepository;
	private final PostMongoRepository postRepository;
	private final MessageMongoRepository messageRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public MessageServiceMongoImpl(MemberMongoRepository memberRepository, PostMongoRepository postRepository, MessageMongoRepository messageRepository, KafkaTemplate<String, Object> kafkaTemplate) {
		this.memberRepository = memberRepository;
		this.postRepository = postRepository;
		this.messageRepository = messageRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public Message createMessage(CreateMessageRequest request) {

		// check post existence
		Optional<PostMongoEntity> existingPost = postRepository.findById(request.getPostId());
		if (existingPost.isEmpty()) {
			LOGGER.error("post not found: " + request.getPostId());
			throw new EntityNotFoundException(request.getPostId(), "Post");
		}


		// check member existence and status
		Optional<MemberMongoEntity> existingSender = memberRepository.findById(request.getSenderId());
		if (existingSender.isEmpty()) {
			LOGGER.error("Sender not found: " + request.getSenderId());
			throw new EntityNotFoundException(request.getSenderId(), "Member");
		}
		Optional<MemberMongoEntity> existingReceiver = memberRepository.findById(request.getReceiverId());
		if (existingReceiver.isEmpty()) {
			LOGGER.error("Receiver not found: " + request.getReceiverId());
			throw new EntityNotFoundException(request.getReceiverId(), "Member");
		}

		if (existingSender.get().getStatus() == MemberStatus.PENDING) {
			LOGGER.error("Sender not verified: " + request.getSenderId());
			throw new MemberNotVerifiedException(request.getSenderId());
		}

		// check participant Id
		if (request.getParticipantId() != request.getSenderId()
				|| request.getParticipantId() != request.getReceiverId()) {
			LOGGER.error("Participant Id mismatch, post not found: " + request.getPostId());
			throw new EntityNotFoundException(request.getPostId(), "Post");
		}

		// save to db
		MessageMongoEntity newEntity = new MessageMongoEntity();
		BeanUtils.copyProperties(request, newEntity);
		newEntity.setId(UUID.randomUUID());
		newEntity.setParticipantName(
				request.getParticipantId() == request.getSenderId()?
				existingSender.get().getName() :
				existingReceiver.get().getName()
				);
		newEntity.setSentAt(LocalDateTime.now());

		MessageMongoEntity saved = messageRepository.save(newEntity);

		// kafka
		MessageCreatedEvent messageCreatedEvent = new MessageCreatedEvent();
		BeanUtils.copyProperties(saved, messageCreatedEvent);
		messageCreatedEvent.setId(saved.getId());

		kafkaTemplate.send(KafkaTopics.MESSAGE_CREATED, messageCreatedEvent);

		// return
		return MessageMongoMapper.toMessage(saved);

	}

	@Override
	public GetConversationResponse getConversation(GetConversationRequest request) {
		List<MessageMongoEntity> entities =
				messageRepository.findByPostIdAndParticipantId(request.getPostId(), request.getParticipantId());

		List<Message> allMessages = entities.stream()
				.map(MessageMongoMapper::toMessage)
				.toList();

		UUID nextMsgId = request.getNextMsgId();
		int startIndex = 0;

		// If nextMsgId is provided, find its index
		if (nextMsgId != null) {
			startIndex = -1;
			for (int i = 0; i < allMessages.size(); i++) {
				if (allMessages.get(i).getId().equals(nextMsgId)) {
					startIndex = i;
					break;
				}
			}
			if (startIndex == -1 || startIndex >= allMessages.size() - 1) {
				// nextMsgId not found or no more messages
				return new GetConversationResponse(Collections.emptyList(), request.getPage(), null);
			}
			startIndex++; // start after the given nextMsgId
		}

		int max = request.getLimit() != null ? request.getLimit().intValue() : allMessages.size();
		int endIndex = Math.min(startIndex + max, allMessages.size());

		List<Message> messages = allMessages.subList(startIndex, endIndex);

		// Determine the nextMsgId for pagination
		UUID newNextMsgId = endIndex < allMessages.size() ? allMessages.get(endIndex).getId() : null;

		return new GetConversationResponse(messages, request.getPage(), newNextMsgId);
	}

	@Override
	public Optional<List<Message>> getAll() {
		List<Message> dtoList = messageRepository.findAll().stream().map(MessageMongoMapper::toMessage).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
