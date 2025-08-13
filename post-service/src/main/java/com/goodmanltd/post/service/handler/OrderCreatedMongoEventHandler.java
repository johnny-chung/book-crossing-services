package com.goodmanltd.post.service.handler;

import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.dto.events.PostReservedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.OrderReference;
import com.goodmanltd.core.types.PostStatus;
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

	public OrderCreatedMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, PostMongoRepository postRepository) {
		this.kafkaTemplate = kafkaTemplate;
		this.postRepository = postRepository;
	}

	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCreatedEvent orderCreatedEvent) {
		LOGGER.info("Post service receive new order created event: " + orderCreatedEvent.getId());

		// check if post exist
		Optional<PostMongoEntity> existingPost = postRepository.findById(orderCreatedEvent.getPostRef().getId());

		if (existingPost.isEmpty()) {
			LOGGER.error("Post {} not found", orderCreatedEvent.getPostRef().getId());
			throw new RetryableException("Post " + orderCreatedEvent.getPostRef().getId() + " not found");
		}


		// to-do
		// post got reserved -> saga pattern -> cancel order command

		// update entity using info from order created event
		PostMongoEntity updatedEntity = new PostMongoEntity();

		BeanUtils.copyProperties(existingPost.get(), updatedEntity);
		updatedEntity.setId(existingPost.get().getId());

		OrderReference orderReference = new OrderReference(
				orderCreatedEvent.getId(),
				orderCreatedEvent.getOrderBy()
		);
		updatedEntity.setOrderRef(orderReference);

		updatedEntity.setPostStatus(PostStatus.RESERVED);

		try {

			PostMongoEntity saved = postRepository.save(updatedEntity);

			// kafka
			// issue ->  post-reserved-event-topic
			PostReservedEvent postReservedEvent = new PostReservedEvent();
			BeanUtils.copyProperties(saved, postReservedEvent);

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
