package com.goodmanltd.order.service;

import com.goodmanltd.core.dao.mongo.entity.mapper.MemberMongoMapper;
import com.goodmanltd.core.dao.mongo.entity.mapper.PostMongoMapper;
import com.goodmanltd.core.dto.events.OrderCancelledEvent;
import com.goodmanltd.core.exceptions.*;
import com.goodmanltd.core.types.Order;
import com.goodmanltd.core.dto.events.OrderCompletedEvent;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.OrderMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.order.dto.CancelOrderRequest;
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
import java.util.Objects;
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
		if (existingMemberEntity.get().getReservationCnt().intValue() >= 5
		|| existingMemberEntity.get().getAnnualTotalReservations().intValue() >= 30) {
			LOGGER.error("Reservation Limit Reached: " + request.getMemberId());
			throw new ReservedLimitReachException(request.getMemberId());
		}

		OrderMongoEntity orderEntity = new OrderMongoEntity();
		orderEntity.setId(UUID.randomUUID());
		orderEntity.setOrderBy(MemberMongoMapper.toMemberRef(existingMemberEntity.get()));
		orderEntity.setPostRef(PostMongoMapper.toPostRef(existingPost.get()));
		orderEntity.setCreatedAt(LocalDateTime.now());
		orderEntity.setOrderStatus(OrderStatus.CREATED);

		OrderMongoEntity saved = orderRepository.save(orderEntity);

		// kafka
		// order-created-event
		OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
		BeanUtils.copyProperties(saved, orderCreatedEvent);

		kafkaTemplate.send(KafkaTopics.ORDER_CREATED, orderCreatedEvent);

		LOGGER.info("*** new order id" + saved.getId());

		return OrderMongoMapper.toOrder(saved);
	}

	@Override
	public Order completeOrder(CompleteOrderRequest request, String auth0Id) {
		// check for order existence
		Optional<OrderMongoEntity> existingOrderEntity = orderRepository.findById(request.getOrderId());
		if (existingOrderEntity.isEmpty()) {
			LOGGER.error("Order not found: " + request.getOrderId());
			throw new EntityNotFoundException(request.getOrderId(), "Order");
		}

		if (!Objects.equals(existingOrderEntity.get().getOrderBy().getAuth0Id(), auth0Id) ||
				!Objects.equals(existingOrderEntity.get().getPostRef().getPostBy().getAuth0Id(), auth0Id)) {
			LOGGER.error("Not Authorized: " + request.getOrderId());
			throw new NotAuthorizedException();
		}

		// save to db
		OrderMongoEntity updatedEntity = new OrderMongoEntity();
		BeanUtils.copyProperties(existingOrderEntity.get(), updatedEntity);
		updatedEntity.setOrderStatus(OrderStatus.COMPLETED);
		updatedEntity.setCompletedAt(LocalDateTime.now());
		OrderMongoEntity saved = orderRepository.save(updatedEntity);

		// kafka
		// order-completed-event
		OrderCompletedEvent orderCompletedEvent = new OrderCompletedEvent();
		BeanUtils.copyProperties(saved, orderCompletedEvent);
		kafkaTemplate.send(KafkaTopics.ORDER_COMPLETED, orderCompletedEvent);

		LOGGER.info("*** order id marked completed" + saved.getId());

		return OrderMongoMapper.toOrder(saved);
	}

	@Override
	public Order cancelOrder(CancelOrderRequest request, String auth0Id) {
		// check for order existence
		Optional<OrderMongoEntity> existingOrderEntity = orderRepository.findById(request.getOrderId());
		if (existingOrderEntity.isEmpty()) {
			LOGGER.error("Order not found: " + request.getOrderId());
			throw new EntityNotFoundException(request.getOrderId(), "Order");
		}

		if (!Objects.equals(existingOrderEntity.get().getOrderBy().getAuth0Id(), auth0Id) ||
				!Objects.equals(existingOrderEntity.get().getPostRef().getPostBy().getAuth0Id(), auth0Id)) {
			LOGGER.error("Not Authorized: " + request.getOrderId());
			throw new NotAuthorizedException();
		}

		// save to db
		OrderMongoEntity updatedEntity = new OrderMongoEntity();
		BeanUtils.copyProperties(existingOrderEntity.get(), updatedEntity);
		updatedEntity.setOrderStatus(OrderStatus.CANCELED);
		updatedEntity.setCompletedAt(LocalDateTime.now());
		OrderMongoEntity saved = orderRepository.save(updatedEntity);

		// kafka
		// order-completed-event
		OrderCancelledEvent orderCancelledEvent = new OrderCancelledEvent();
		BeanUtils.copyProperties(saved, orderCancelledEvent);
		kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, orderCancelledEvent);

		LOGGER.info("*** order id marked cancelled" + saved.getId());

		return OrderMongoMapper.toOrder(saved);
	}


	@Override
	public Optional<Order> findByOrderId(UUID orderId) {
		return orderRepository.findById(orderId).map(OrderMongoMapper::toOrder);
	}

	@Override
	public Optional<List<Order>> findByPostId(UUID postId) {
		List<OrderMongoEntity> entities = orderRepository.findByPostRef_Id(postId);
		List<Order> dtoList = entities.stream().map(OrderMongoMapper::toOrder).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}

	@Override
	public Optional<List<Order>> findByMemberId(UUID memberId) {
		List<OrderMongoEntity> entities = orderRepository.findByPostRef_Postby_Id(memberId);
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
