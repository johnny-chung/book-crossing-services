package com.goodmanltd.order.service.handler;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dto.events.MemberCreatedEvent;
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
@KafkaListener(topics= KafkaTopics.MEMBER_CREATED)
public class MemberCreatedMongoEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final MemberMongoRepository memberRepository;

	public MemberCreatedMongoEventHandler(MemberMongoRepository memberRepository) {
		this.memberRepository  = memberRepository;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload MemberCreatedEvent memberCreatedEvent) {
		LOGGER.info("Order service receive member created event: " + memberCreatedEvent.getId());

		Optional<MemberMongoEntity> existingRecord = memberRepository.findById(memberCreatedEvent.getId());

		if (existingRecord.isPresent()) {
			LOGGER.info("Found a duplicate member id: {}", existingRecord.get().getId());
			// do nothing
			return;
		}

		MemberMongoEntity entity = new MemberMongoEntity();
		BeanUtils.copyProperties(memberCreatedEvent, entity);
		entity.setId(memberCreatedEvent.getId());

		// version control
		try {
			memberRepository.save(entity);

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
