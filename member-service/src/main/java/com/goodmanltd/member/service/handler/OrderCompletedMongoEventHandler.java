package com.goodmanltd.member.service.handler;

import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.core.dto.events.OrderCompletedEvent;
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
@KafkaListener(topics= KafkaTopics.ORDER_COMPLETED)
public class OrderCompletedMongoEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final OrderMongoRepository orderRepository;

	public OrderCompletedMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, OrderMongoRepository orderRepository) {
		this.kafkaTemplate = kafkaTemplate;
		this.orderRepository = orderRepository;
	}

	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCompletedEvent orderCompletedEvent) {
		LOGGER.info("Member service receive order completed event: " + orderCompletedEvent.getId());

		// check if post exist
		Optional<OrderMongoEntity> existingRecord = orderRepository.findById(orderCompletedEvent.getId());

		if (existingRecord.isEmpty()) {
			LOGGER.error("Order {} not found", orderCompletedEvent.getId());
			throw new RetryableException("Member Service: Order " + orderCompletedEvent.getId() + " not found");
		}


		// update entity using info from order created event
		OrderMongoEntity updatedEntity = new OrderMongoEntity();
		BeanUtils.copyProperties(existingRecord.get(), updatedEntity);

		updatedEntity.setId(existingRecord.get().getId());
		updatedEntity.setOrderStatus(orderCompletedEvent.getOrderStatus());
		updatedEntity.setCompletedAt(orderCompletedEvent.getCompletedAt());

		try {

			orderRepository.save(updatedEntity);

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
