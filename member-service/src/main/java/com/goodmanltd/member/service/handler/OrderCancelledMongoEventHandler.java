package com.goodmanltd.member.service.handler;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.core.dto.events.MemberUpdatedEvent;
import com.goodmanltd.core.dto.events.OrderCancelledEvent;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.dto.events.mapper.OrderEventMapper;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.OrderStatus;
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
	private final OrderMongoRepository orderRepository;
	private final MemberMongoRepository memberRepo;

	public OrderCancelledMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, OrderMongoRepository orderRepository, MemberMongoRepository memberRepo) {
		this.kafkaTemplate = kafkaTemplate;
		this.orderRepository = orderRepository;
		this.memberRepo = memberRepo;
	}

	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCancelledEvent orderCancelledEvent) {


		LOGGER.info("Member service receive order cancelled event: " + orderCancelledEvent.getId());

		// check if order exist
		Optional<OrderMongoEntity> existingOrder = orderRepository.findById(orderCancelledEvent.getId());

		if (existingOrder.isEmpty()) {
			LOGGER.error("Order {} not found", orderCancelledEvent.getId());
			throw new RetryableException("Member Service: Order " + orderCancelledEvent.getId() + " not found");
		}

		// check if member exist
		Optional<MemberMongoEntity> existingMember = memberRepo.findById(orderCancelledEvent.getOrderBy().getId());
		if (existingMember.isEmpty()) {
			LOGGER.error("Member {} not found", orderCancelledEvent.getOrderBy().getId());
			throw new RetryableException("Member Service: Member " +
					orderCancelledEvent.getOrderBy().getId() + " not found");
		}


		// update member db
		MemberMongoEntity updatedMemberEntity = new MemberMongoEntity();
		BeanUtils.copyProperties(existingMember.get(), updatedMemberEntity);
		updatedMemberEntity.setId(orderCancelledEvent.getOrderBy().getId());
		updatedMemberEntity.setReservationCnt(existingMember.get().getReservationCnt().intValue() - 1);
		updatedMemberEntity.setAnnualTotalReservations(
				existingMember.get().getAnnualTotalReservations().intValue() - 1);



		// update order db
		OrderMongoEntity newEntity = new OrderMongoEntity();
		BeanUtils.copyProperties(existingOrder.get(), newEntity);
		newEntity.setOrderStatus(OrderStatus.CANCELED);

		try {

			MemberMongoEntity saved = memberRepo.save(updatedMemberEntity);
			orderRepository.save(newEntity);

			// kafka
			MemberUpdatedEvent memberUpdatedEvent = new MemberUpdatedEvent();
			BeanUtils.copyProperties(saved, memberUpdatedEvent);
			kafkaTemplate.send(KafkaTopics.MEMBER_UPDATED, memberUpdatedEvent);
			LOGGER.info("member service receive order cancelled event and updated member");

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
