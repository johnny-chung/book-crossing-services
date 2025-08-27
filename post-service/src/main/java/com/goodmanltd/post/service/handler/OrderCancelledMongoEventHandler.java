package com.goodmanltd.post.service.handler;

import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.OrderCancelledEvent;
import com.goodmanltd.core.dto.events.PostUpdatedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
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
@KafkaListener(topics= KafkaTopics.ORDER_CANCELLED)
public class OrderCancelledMongoEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final PostMongoRepository postRepository;

	public OrderCancelledMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, PostMongoRepository postRepository) {
		this.kafkaTemplate = kafkaTemplate;
		this.postRepository = postRepository;
	}

	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCancelledEvent orderCancelledEvent) {
		LOGGER.info("Post service receive order cancel event: " + orderCancelledEvent.getId());

		// check if post exist
		Optional<PostMongoEntity> existingRecord = postRepository.findById(
				orderCancelledEvent.getPostRef().getId());

		if (existingRecord.isEmpty()) {
			LOGGER.error("Post {} not found", orderCancelledEvent.getPostRef().getId());
			throw new RetryableException("Post " + orderCancelledEvent.getPostRef().getId() + " not found");
		}

		// to-do
		// check order id record is valid

		// update entity using info from order created event
		PostMongoEntity updatedEntity = new PostMongoEntity();
		BeanUtils.copyProperties(existingRecord.get(), updatedEntity);

		updatedEntity.setId(existingRecord.get().getId());
		updatedEntity.setOrderRef(null);
		updatedEntity.setPostStatus(PostStatus.AVAILABLE);

		try {

			PostMongoEntity saved = postRepository.save(updatedEntity);

			// kafka
			// issue ->  post-completed-event-topic
			PostUpdatedEvent postUpdatedEvent = new PostUpdatedEvent();
			BeanUtils.copyProperties(saved, postUpdatedEvent);
			postUpdatedEvent.setId(saved.getId());

			kafkaTemplate.send(KafkaTopics.POST_UPDATED, postUpdatedEvent);
			LOGGER.info("Post service issues Post Updated event upon order completion: " + postUpdatedEvent.getId());

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
