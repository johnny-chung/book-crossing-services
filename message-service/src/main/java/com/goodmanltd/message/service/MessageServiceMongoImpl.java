package com.goodmanltd.message.service;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.MessageMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.MemberMongoMapper;
import com.goodmanltd.core.dao.mongo.entity.mapper.MessageMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.MessageMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.MessageCreatedEvent;
import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.exceptions.MemberNotVerifiedException;
import com.goodmanltd.core.exceptions.NotAuthorizedException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.core.types.Message;
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
	public Message createMessage(CreateMessageRequest request, String auth0Id) {

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

		// check authorization
		if (!Objects.equals(existingSender.get().getAuth0Id(), auth0Id) &&
				!Objects.equals(existingReceiver.get().getAuth0Id(), auth0Id)) {
			LOGGER.error("Not authorized to send message");
			throw new NotAuthorizedException();
		}

		// check participant Id
		if (request.getParticipantId().equals(request.getSenderId())
				&& request.getParticipantId().equals(request.getReceiverId())) {
			LOGGER.error("Participant Id mismatch: " + request.getParticipantId());
			throw new NotAuthorizedException();
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
				request.getParticipantId().equals(existingSender.get().getId())?
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

		if (entities.isEmpty()) {
			return new GetConversationResponse(Collections.emptyList(), 1, null, null);
		}

		// Sort messages by sentAt ascending (oldest → newest)
		List<Message> allMessages = entities.stream()
				.map(MessageMongoMapper::toMessage)
				.sorted(Comparator.comparing(Message::getSentAt).reversed())
				.toList();

		UUID nextMsgId = request.getNextMsgId();
		UUID startMsgId = request.getStartMsgId();
		List<Message> messages;
		UUID newNextMsgId = null;
		UUID newStartMsgId = null;

		int limit = request.getLimit() != null ? request.getLimit().intValue() : 25;

		if (request.getPage().intValue() == 1) {
			// Page 1: newest messages
			if (nextMsgId != null) {
				// Return messages from start until nextMsgId (exclusive)
				int endIndex = -1;
				for (int i = 0; i < allMessages.size(); i++) {
					if (allMessages.get(i).getId().equals(nextMsgId)) {
						endIndex = i;
						break;
					}
				}
				if (endIndex == -1) endIndex = allMessages.size();
				messages = allMessages.subList(0, endIndex);

				newNextMsgId = nextMsgId; // keep the provided nextMsgId
			} else {
				// nextMsgId == null → return newest messages up to limit
				int totalMessages = allMessages.size();
				int endIndex = Math.min(limit, totalMessages);
				messages = allMessages.subList(0, endIndex);

				// If older messages remain before this batch
				newNextMsgId = (endIndex < totalMessages) ?
						allMessages.get(endIndex).getId() : null;
			}

			if (!messages.isEmpty()) {
				newStartMsgId = messages.getFirst().getId();
			}

		} else {
			// Page >= 2: use startMsgId + limit
			int startIndex = 0;

			if (startMsgId != null) {
				// find start index after the startMsgId
				startIndex = -1;
				for (int i = 0; i < allMessages.size(); i++) {
					if (allMessages.get(i).getId().equals(startMsgId)) {
						startIndex = i + 1;
						break;
					}
				}
				if (startIndex == -1 || startIndex >= allMessages.size()) {
					return new GetConversationResponse(Collections.emptyList(),
							request.getPage().intValue(), startMsgId, null);
				}
			}

			int endIndex = Math.min(startIndex + limit, allMessages.size());
			messages = allMessages.subList(startIndex, endIndex);

			// nextMsgId = immediate next message after the batch, or null if end
			newNextMsgId = endIndex < allMessages.size() ? allMessages.get(endIndex).getId() : null;
			if (!messages.isEmpty()) {
				newStartMsgId = messages.getFirst().getId(); // last message in this batch
			}
		}

		return new GetConversationResponse(
				messages, request.getPage().intValue(), newStartMsgId, newNextMsgId
		);
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

		return new GetParticipantListByPost(postId, existingPost.get().getBookRef() ,participantRefs);
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
				.map(postId -> postRepository.findById(postId)
						.map(existingPost -> {
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
							elem.setBookRef(existingPost.getBookRef());
							elem.setParticipants(participantRefs);
							return elem;
						})
				)
				.filter(Optional::isPresent)        // remove empty optionals
				.map(Optional::get)                 // unwrap
				.collect(Collectors.toList());
	}

	@Override
	public Optional<List<Message>> getAll() {
		List<Message> dtoList = messageRepository.findAll().stream().map(MessageMongoMapper::toMessage).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
