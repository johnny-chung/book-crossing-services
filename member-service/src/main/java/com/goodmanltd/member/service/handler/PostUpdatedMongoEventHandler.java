package com.goodmanltd.member.service.handler;

import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.PostUpdatedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
@KafkaListener(topics= KafkaTopics.POST_UPDATED)
public class PostUpdatedMongoEventHandler {


	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final PostMongoRepository postRepository;

	public PostUpdatedMongoEventHandler(PostMongoRepository postRepository) {
		this.postRepository = postRepository;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload PostUpdatedEvent postUpdatedEvent) {
		LOGGER.info("Member service receive post updated event: " + postUpdatedEvent.getId());

		Optional<PostMongoEntity> existingRecord = postRepository.findById(postUpdatedEvent.getId());

		if (existingRecord.isEmpty()) {
			LOGGER.error("Post {} not found ", postUpdatedEvent.getId());
			throw new RetryableException("Post "+ postUpdatedEvent.getId() + " not found");
		}

		PostMongoEntity entity = new PostMongoEntity();
		BeanUtils.copyProperties(existingRecord.get(), entity);
		BeanUtils.copyProperties(postUpdatedEvent, entity);
		entity.setId(postUpdatedEvent.getId());

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
