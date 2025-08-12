package com.goodmanltd.post.service.handler;

import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.dto.events.PostReservedEvent;
import com.goodmanltd.core.dto.events.PostUpdatedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.post.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.post.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.post.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.post.dao.mongo.repository.PostMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Profile("mongo")
@Component
@KafkaListener(topics= KafkaTopics.ORDER_CREATED)
public class OrderCreatedMongoEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final PostMongoRepository postRepository;
	private final MemberMongoRepository memberRepo;

	public OrderCreatedMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, PostMongoRepository postRepository, MemberMongoRepository memberRepo) {
		this.kafkaTemplate = kafkaTemplate;
		this.postRepository = postRepository;
		this.memberRepo = memberRepo;
	}

	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCreatedEvent orderCreatedEvent) {
		LOGGER.info("Post service receive new order created event: " + orderCreatedEvent.getOrderId());

		// check if post exist
		Optional<PostMongoEntity> existingRecord = postRepository.findById(orderCreatedEvent.getPostId());

		if (existingRecord.isEmpty()) {
			LOGGER.error("Post {} not found", orderCreatedEvent.getPostId());
			throw new RetryableException("Post " + orderCreatedEvent.getPostId() + " not found");
		}

		// member name lookup
		Optional<MemberMongoEntity> member = memberRepo.findById(orderCreatedEvent.getMemberId());


		// to-do
		// post got reserved -> saga pattern -> cancel order command

		// update entity using info from order created event
		PostMongoEntity updatedEntity = new PostMongoEntity();
		BeanUtils.copyProperties(existingRecord.get(), updatedEntity);

		updatedEntity.setId(existingRecord.get().getId());
		updatedEntity.setOrderId(orderCreatedEvent.getOrderId());
		updatedEntity.setReservedBy(orderCreatedEvent.getMemberId());
		updatedEntity.setReservedName(member.isPresent()? member.get().getName() : "");
		updatedEntity.setPostStatus(PostStatus.RESERVED);
		// simplifier operation, directly set order status to pending
		updatedEntity.setOrderStatus(OrderStatus.PENDING);


		try {

			PostMongoEntity saved = postRepository.save(updatedEntity);

			// kafka
			// issue ->  post-reserved-event-topic
			PostReservedEvent postReservedEvent = new PostReservedEvent();
			BeanUtils.copyProperties(saved, postReservedEvent);
			postReservedEvent.setOrderStatus(OrderStatus.PENDING);

			kafkaTemplate.send(KafkaTopics.POST_RESERVED, postReservedEvent);
			LOGGER.info("Post service issues Post Reserved event: " + postReservedEvent.getId());

		} catch (OptimisticLockingFailureException ex) {
			LOGGER.error(ex.getMessage());
			throw new RetryableException(ex);
		}
		catch (DataIntegrityViolationException ex) {
			LOGGER.error(ex.getMessage());
			throw new NotRetryableException(ex);
		}




	}
}
