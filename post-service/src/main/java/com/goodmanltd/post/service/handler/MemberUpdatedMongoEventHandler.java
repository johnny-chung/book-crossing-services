package com.goodmanltd.post.service.handler;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dto.events.MemberUpdatedEvent;
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
@KafkaListener(topics= KafkaTopics.MEMBER_UPDATED)
public class MemberUpdatedMongoEventHandler {


	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final MemberMongoRepository memberRepo;

	public MemberUpdatedMongoEventHandler(MemberMongoRepository memberRepo) {
		this.memberRepo = memberRepo;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload MemberUpdatedEvent memberUpdatedEvent) {
		LOGGER.info("Post service receive member updated event: " + memberUpdatedEvent.getId());

		Optional<MemberMongoEntity> existingRecord = memberRepo.findById(memberUpdatedEvent.getId());

		if (existingRecord.isEmpty()) {
			LOGGER.error("Member {} not found ", memberUpdatedEvent.getId());
			throw new RetryableException("Member "+ memberUpdatedEvent.getId() + " not found");
		}

		MemberMongoEntity entity = new MemberMongoEntity();
		BeanUtils.copyProperties(existingRecord.get(), entity);
		BeanUtils.copyProperties(memberUpdatedEvent, entity);
		entity.setId(memberUpdatedEvent.getId());

		// version control
		try {
			memberRepo.save(entity);

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
