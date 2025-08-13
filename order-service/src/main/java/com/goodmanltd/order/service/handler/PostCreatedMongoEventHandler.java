package com.goodmanltd.order.service.handler;

import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.PostCreatedEvent;
import com.goodmanltd.core.dto.events.mapper.PostEventMapper;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Profile("mongo")
@Component
@KafkaListener(topics= KafkaTopics.POST_CREATED)
public class PostCreatedMongoEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final PostMongoRepository postRepository;

	public PostCreatedMongoEventHandler(PostMongoRepository postRepository) {
		this.postRepository = postRepository;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload PostCreatedEvent postCreatedEvent) {
		LOGGER.info("Order service receive new post created event: " + postCreatedEvent.getId());

		Optional<PostMongoEntity> existingRecord = postRepository.findById(postCreatedEvent.getId());

		if (existingRecord.isPresent()) {
			LOGGER.info("Found a duplicate post id: {}", existingRecord.get().getId());
			return;
		}

		PostMongoEntity entity = PostEventMapper.createdEventToEntity(postCreatedEvent);

		// version control
		try {
			postRepository.save(entity);

		} catch (OptimisticLockingFailureException ex) {
			// version not match -> retry
			LOGGER.error(ex.getMessage());
			throw new RetryableException(ex);
		}
		catch (DataIntegrityViolationException ex) {
			LOGGER.error(ex.getMessage());
			throw new NotRetryableException(ex);
		}
	}
}
