package com.goodmanltd.member.service.handler;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.core.dto.events.MemberUpdatedEvent;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.dto.events.mapper.OrderEventMapper;
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
	private final OrderMongoRepository orderRepository;
	private final MemberMongoRepository memberRepo;

	public OrderCreatedMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, OrderMongoRepository orderRepository, MemberMongoRepository memberRepo) {
		this.kafkaTemplate = kafkaTemplate;
		this.orderRepository = orderRepository;
		this.memberRepo = memberRepo;
	}

	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCreatedEvent orderCreatedEvent) {


		LOGGER.info("Member service receive order created event: " + orderCreatedEvent.getId());

		// check if order exist
		Optional<OrderMongoEntity> existingRecord = orderRepository.findById(orderCreatedEvent.getId());

		if (existingRecord.isPresent()) {
			LOGGER.error("Order {} duplicated", orderCreatedEvent.getId());
			throw new RetryableException("Member Service: Order " + orderCreatedEvent.getId() + " duplicated");
		}


		// check if member exist
		Optional<MemberMongoEntity> existingMember = memberRepo.findById(orderCreatedEvent.getOrderBy().getId());
		if (existingMember.isEmpty()) {
			LOGGER.error("Member {} not found", orderCreatedEvent.getOrderBy().getId());
			// to-do
		} else {
			// update member db
			MemberMongoEntity updatedMemberEntity = new MemberMongoEntity();
			BeanUtils.copyProperties(existingMember.get(), updatedMemberEntity);
			updatedMemberEntity.setId(orderCreatedEvent.getOrderBy().getId());
			updatedMemberEntity.setReservationCnt(((MemberMongoEntity)existingMember.get()).getReservationCnt().intValue() + 1);
			updatedMemberEntity.setAnnualTotalReservations(((MemberMongoEntity)existingMember.get()).getAnnualTotalReservations().intValue() + 1);


			MemberMongoEntity saved = memberRepo.save(updatedMemberEntity);

			// kafka
			MemberUpdatedEvent memberUpdatedEvent = new MemberUpdatedEvent();
			BeanUtils.copyProperties(saved, memberUpdatedEvent);
			kafkaTemplate.send(KafkaTopics.MEMBER_UPDATED, memberUpdatedEvent);

			LOGGER.info("member service receive order create event and updated member");
		}



		// update entity using info from order created event
		OrderMongoEntity newEntity = OrderEventMapper.createdEventToEntity(orderCreatedEvent);

		try {

			orderRepository.save(newEntity);

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
