package com.goodmanltd.message.service;

import com.goodmanltd.core.dao.mongo.entity.mapper.MemberMongoMapper;
import com.goodmanltd.core.exceptions.NotAuthorizedException;
import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.Message;
import com.goodmanltd.core.dto.events.MessageCreatedEvent;
import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.exceptions.MemberNotVerifiedException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.MessageMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.MessageMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.MessageMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.message.dto.CreateMessageRequest;
import com.goodmanltd.message.dto.GetConversationRequest;
import com.goodmanltd.message.dto.GetConversationResponse;
import com.goodmanltd.message.dto.GetParticipantListByPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Profile("mongo")
@Service
public class MessageServiceMongoImpl implements MessageService{

	private final MongoTemplate mongoTemplate;
	private final MemberMongoRepository memberRepository;
	private final PostMongoRepository postRepository;
	private final MessageMongoRepository messageRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public MessageServiceMongoImpl(MongoTemplate mongoTemplate, MemberMongoRepository memberRepository, PostMongoRepository postRepository, MessageMongoRepository messageRepository, KafkaTemplate<String, Object> kafkaTemplate) {
		this.mongoTemplate = mongoTemplate;
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
		MemberReference senderRef = MemberMongoMapper.toMemberRef(existingSender.get());
		MemberReference receiverRef = MemberMongoMapper.toMemberRef(existingReceiver.get());

		newEntity.setSender(senderRef);
		newEntity.setReceiver(receiverRef);
		newEntity.setParticipant(
				request.getParticipantId() == existingSender.get().getId()?
						senderRef:
						receiverRef
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
	public GetParticipantListByPost getParticipantListByPost(UUID postId, String requestAuth0Id) {

		// check post existence
		Optional<PostMongoEntity> existingPost = postRepository.findById(postId);
		if (existingPost.isEmpty()) {
			LOGGER.error("post not found: " + postId);
			throw new EntityNotFoundException(postId, "Post");
		}
		// check authorization
		if (!Objects.equals(existingPost.get().getPostBy().getAuth0Id(), requestAuth0Id)){
			LOGGER.error("Not Authorized to view : " +postId);
			throw new NotAuthorizedException();
		}

		List<UUID> participantIds = mongoTemplate.query(MessageMongoEntity.class)
				.distinct("participant.id")
				.matching(Query.query(Criteria.where("postId").is(postId)))
				.as(UUID.class)
				.all();

		// Optional optimization: fetch all members in one query
		List<MemberMongoEntity> members = memberRepository.findAllById(participantIds);

		List<MemberReference> participantRefs = members.stream()
				.map(MemberMongoMapper::toMemberRef)
				.toList();

		return new GetParticipantListByPost(postId, participantRefs);
	}

	@Override
	public List<GetParticipantListByPost> getParticipantListByAuth0Id(String requestAuth0Id) {
		List<UUID> postIds = mongoTemplate.query(MessageMongoEntity.class)
				.distinct("postId")
				.matching(Query.query(
						new Criteria().orOperator(
								Criteria.where("sender.auth0Id").is(requestAuth0Id),
								Criteria.where("receiver.auth0Id").is(requestAuth0Id)
						)
				))
				.as(UUID.class)
				.all();

		return postIds.stream()
				.map(postId -> {
					// Get participant IDs for this post
					List<UUID> participantIds = mongoTemplate.query(MessageMongoEntity.class)
							.distinct("participant.id")
							.matching(Query.query(Criteria.where("postId").is(postId)))
							.as(UUID.class)
							.all();

					// Fetch member entities
					List<MemberMongoEntity> members = memberRepository.findAllById(participantIds);

					// Map to references
					List<MemberReference> participantRefs = members.stream()
							.map(MemberMongoMapper::toMemberRef)
							.collect(Collectors.toList());

					// Build response element
					GetParticipantListByPost elem = new GetParticipantListByPost();
					elem.setPostId(postId);
					elem.setParticipants(participantRefs);
					return elem;
				})
				.collect(Collectors.toList());
	}

	@Override
	public Optional<List<Message>> getAll() {
		List<Message> dtoList = messageRepository.findAll().stream().map(MessageMongoMapper::toMessage).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
