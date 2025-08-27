package com.goodmanltd.order.service.handler;

import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.OrderPendingEvent;
import com.goodmanltd.core.dto.events.PostReservedEvent;
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
@KafkaListener(topics= KafkaTopics.POST_RESERVED)
public class PostReservedMongoEventHandler {


	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final PostMongoRepository postRepository;
	private final OrderMongoRepository orderRepository;

	public PostReservedMongoEventHandler(KafkaTemplate<String, Object> kafkaTemplate, PostMongoRepository postRepository, OrderMongoRepository orderRepository) {
		this.kafkaTemplate = kafkaTemplate;
		this.postRepository = postRepository;
		this.orderRepository = orderRepository;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload PostReservedEvent postReservedEvent) {
		LOGGER.info("Order service receive post reserved event: " + postReservedEvent.getId());

		// update post db
		Optional<PostMongoEntity> existingRecord = postRepository.findById(postReservedEvent.getId());

		if (existingRecord.isEmpty()) {
			LOGGER.error("Post {} not found ", postReservedEvent.getId());
			throw new RetryableException("Post "+ postReservedEvent.getId() + " not found");
		}

		PostMongoEntity postEntity = new PostMongoEntity();
		BeanUtils.copyProperties(existingRecord.get(), postEntity);
		BeanUtils.copyProperties(postReservedEvent, postEntity);
		postEntity.setId(postReservedEvent.getId());


		// update order status to "Pending"
		Optional<OrderMongoEntity> existingOrder = orderRepository.findById(
				postReservedEvent.getOrderRef().getId());
		if (existingOrder.isEmpty()) {
			LOGGER.error("Order {} not found ", postReservedEvent.getOrderRef().getId());
			throw new RetryableException("Order "+ postReservedEvent.getOrderRef().getId() + " not found");
		}

		OrderMongoEntity orderEntity = new OrderMongoEntity();
		BeanUtils.copyProperties(existingOrder.get(), orderEntity);
		orderEntity.setId(existingOrder.get().getId());
		orderEntity.setOrderStatus(OrderStatus.PENDING);


		// version control
		try {
			postRepository.save(postEntity);
			orderRepository.save(orderEntity);

		} catch (OptimisticLockingFailureException ex) {
			// version not match -> retry
			LOGGER.error(ex.getMessage());
			throw new RetryableException(ex);
		}
		catch (DataIntegrityViolationException ex) {
			LOGGER.error(ex.getMessage());
			throw new NotRetryableException(ex);
		}

		// kafka
		// order-pending-event
		OrderPendingEvent orderPendingEvent = new OrderPendingEvent();
		BeanUtils.copyProperties(orderEntity, orderPendingEvent);
		kafkaTemplate.send(KafkaTopics.ORDER_PENDING, orderPendingEvent);
		LOGGER.info("Order service issues Order pending event: " + orderPendingEvent.getId());

	}
}
