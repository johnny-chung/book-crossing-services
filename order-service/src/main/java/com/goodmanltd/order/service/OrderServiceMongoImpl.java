package com.goodmanltd.order.service;

import com.goodmanltd.core.types.Order;
import com.goodmanltd.core.dto.events.OrderCompletedEvent;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.exceptions.MemberNotVerifiedException;
import com.goodmanltd.core.exceptions.PostReservedException;
import com.goodmanltd.core.exceptions.ReservedLimitReachException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.order.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.order.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.order.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.order.dao.mongo.entity.mapper.OrderMongoMapper;
import com.goodmanltd.order.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.order.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.order.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.order.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.order.dto.CompleteOrderRequest;
import com.goodmanltd.order.dto.CreateOrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Service
public class OrderServiceMongoImpl implements OrderService {
	private final PostMongoRepository postRepository;
	private final OrderMongoRepository orderRepository;
	private final MemberMongoRepository memberRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

//	@Value("${app.kafka.topics.orderCreated}")
//	private String orderCreatedTopic;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public OrderServiceMongoImpl(BookMongoRepository bookRepository, PostMongoRepository postRepository, OrderMongoRepository orderRepository, MemberMongoRepository memberRepostory, KafkaTemplate<String, Object> kafkaTemplate) {
		this.postRepository = postRepository;

		this.orderRepository = orderRepository;
		this.memberRepository = memberRepostory;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public Order createOrder(CreateOrderRequest request) {

		// check post existence
		Optional<PostMongoEntity> existingPost = postRepository.findById(request.getPostId());
		if (existingPost.isEmpty()) {
			LOGGER.error("post not found: " + request.getPostId());
			throw new EntityNotFoundException(request.getPostId(), "Post");
		}
		// check if post is available
		if (existingPost.get().getPostStatus() != PostStatus.AVAILABLE) {
			LOGGER.error("book is reserved: " + request.getPostId());
			throw new PostReservedException(request.getPostId());
		}

		// check member existence
		Optional<MemberMongoEntity> existingMemberEntity = memberRepository.findById(request.getMemberId());
		if (existingMemberEntity.isEmpty()) {
			LOGGER.error("Member not found: " + request.getMemberId());
			throw new EntityNotFoundException(request.getMemberId(), "Member");
		}

		// check member status
		if (existingMemberEntity.get().getStatus() == MemberStatus.PENDING) {
			LOGGER.error("Member not verified: " + request.getMemberId());
			throw new MemberNotVerifiedException(request.getMemberId());
		}

		// check reservation limit
		if (existingMemberEntity.get().getReservedCount().intValue() >= 5) {
			LOGGER.error("Reservation Limit Reached: " + request.getMemberId());
			throw new ReservedLimitReachException(request.getMemberId());
		}

		OrderMongoEntity orderEntity = new OrderMongoEntity();
		orderEntity.setId(UUID.randomUUID());
		orderEntity.setMemberId(request.getMemberId());
		orderEntity.setPostId(request.getPostId());
		orderEntity.setCreatedAt(LocalDateTime.now());
		orderEntity.setOrderStatus(OrderStatus.CREATED);

		OrderMongoEntity saved = orderRepository.save(orderEntity);

		// kafka
		// order-created-event
		OrderCreatedEvent createNewOrder = new OrderCreatedEvent(
				saved.getId(),
				saved.getPostId(),
				saved.getMemberId(),
				saved.getCreatedAt()
		);

		kafkaTemplate.send(KafkaTopics.ORDER_CREATED, createNewOrder);

		LOGGER.info("*** new order id" + saved.getId());

		return OrderMongoMapper.toOrder(saved);
	}

	@Override
	public Order completeOrder(CompleteOrderRequest request) {
		// check for order existence
		Optional<OrderMongoEntity> existingOrderEntity = orderRepository.findById(request.getOrderId());
		if (existingOrderEntity.isEmpty()) {
			LOGGER.error("Order not found: " + request.getOrderId());
			throw new EntityNotFoundException(request.getOrderId(), "Order");
		}

		// save to db
		OrderMongoEntity updatedEntity = new OrderMongoEntity();
		BeanUtils.copyProperties(existingOrderEntity.get(), updatedEntity);
		updatedEntity.setOrderStatus(OrderStatus.COMPLETED);
		updatedEntity.setCompletedAt(LocalDateTime.now());
		OrderMongoEntity saved = orderRepository.save(updatedEntity);

		// kafka
		// order-completed-event
		OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent(
				saved.getId(),
				saved.getPostId(),
				saved.getMemberId(),
				saved.getOrderStatus(),
				saved.getCompletedAt()
		);
		kafkaTemplate.send(KafkaTopics.ORDER_COMPLETED, orderCompletedEvent);

		LOGGER.info("*** order id marked completed" + saved.getId());

		return OrderMongoMapper.toOrder(saved);
	}


	@Override
	public Optional<Order> findByOrderId(UUID orderId) {
		return orderRepository.findById(orderId).map(OrderMongoMapper::toOrder);
	}

	@Override
	public Optional<List<Order>> findByPostId(UUID postId) {
		List<OrderMongoEntity> entities = orderRepository.findByPostId(postId);
		List<Order> dtoList = entities.stream().map(OrderMongoMapper::toOrder).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}

	@Override
	public Optional<List<Order>> findByMemberId(UUID memberId) {
		List<OrderMongoEntity> entities = orderRepository.findByMemberId(memberId);
		List<Order> dtoList = entities.stream().map(OrderMongoMapper::toOrder).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}

	@Override
	public Optional<List<Order>> findByAuth0Id(String auth0Id) {
		Optional<MemberMongoEntity> existingMember = memberRepository.findByAuth0Id(auth0Id);
		if (existingMember.isEmpty()) {
			LOGGER.error("Member not found: " + auth0Id);
			throw new EntityNotFoundException(auth0Id, "Member");
		}
		UUID memberId = existingMember.get().getId();

		return findByMemberId(memberId);
	}

	@Override
	public Optional<List<Order>> findAll() {
		List<Order> dtoList = orderRepository.findAll().stream().map(OrderMongoMapper::toOrder).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
