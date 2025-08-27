package com.goodmanltd.order.service;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.MemberMongoMapper;
import com.goodmanltd.core.dao.mongo.entity.mapper.OrderMongoMapper;
import com.goodmanltd.core.dao.mongo.entity.mapper.PostMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.OrderMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.core.dto.events.OrderCancelledEvent;
import com.goodmanltd.core.dto.events.OrderCompletedEvent;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.exceptions.*;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.core.types.Order;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.order.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.*;

@Profile("mongo")
@Service
public class OrderServiceMongoImpl implements OrderService {
	private final PostMongoRepository postRepository;
	private final OrderMongoRepository orderRepository;
	private final MemberMongoRepository memberRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final MongoTemplate mongoTemplate;

//	@Value("${app.kafka.topics.orderCreated}")
//	private String orderCreatedTopic;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public OrderServiceMongoImpl(BookMongoRepository bookRepository, PostMongoRepository postRepository, OrderMongoRepository orderRepository, MemberMongoRepository memberRepostory, KafkaTemplate<String, Object> kafkaTemplate, MongoTemplate mongoTemplate) {
		this.postRepository = postRepository;

		this.orderRepository = orderRepository;
		this.memberRepository = memberRepostory;
		this.kafkaTemplate = kafkaTemplate;
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public CreateOrderResponse createOrder(CreateOrderRequest request) {

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


		CreateOrderResponse response = new CreateOrderResponse();
		BeanUtils.copyProperties(OrderMongoMapper.toOrder(saved), response);
		return response;
	}

	@Override
	public CompleteOrderResponse completeOrder(CompleteOrderRequest request, String auth0Id) {
		// check for order existence
		Optional<OrderMongoEntity> existingOrderEntity = orderRepository.findById(request.getId());
		if (existingOrderEntity.isEmpty()) {
			LOGGER.error("Order not found: " + request.getId());
			throw new EntityNotFoundException(request.getId(), "Order");
		}

		if (!Objects.equals(existingOrderEntity.get().getOrderBy().getAuth0Id(), auth0Id) &&
				!Objects.equals(existingOrderEntity.get().getPostRef().getPostBy().getAuth0Id(), auth0Id)) {
			LOGGER.error("Not Authorized: " + request.getId());
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


		CompleteOrderResponse completeOrderResponse = new CompleteOrderResponse();
		BeanUtils.copyProperties(OrderMongoMapper.toOrder(saved), completeOrderResponse);
		return completeOrderResponse;
	}

	@Override
	public CancelOrderResponse cancelOrder(CancelOrderRequest request, String auth0Id) {
		// check for order existence
		Optional<OrderMongoEntity> existingOrderEntity = orderRepository.findById(request.getId());
		if (existingOrderEntity.isEmpty()) {
			LOGGER.error("Order not found: " + request.getId());
			throw new EntityNotFoundException(request.getId(), "Order");
		}

		if (!Objects.equals(existingOrderEntity.get().getOrderBy().getAuth0Id(), auth0Id) &&
				!Objects.equals(existingOrderEntity.get().getPostRef().getPostBy().getAuth0Id(), auth0Id)) {
			LOGGER.error("Not Authorized: " + auth0Id);
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


		CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
		BeanUtils.copyProperties(OrderMongoMapper.toOrder(saved), cancelOrderResponse);
		return cancelOrderResponse;
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
	public List<Order> findByMemberId(UUID memberId) {
		List<OrderMongoEntity> entities = orderRepository.findByPostRef_PostBy_Id(memberId);
		return entities.stream().map(OrderMongoMapper::toOrder).toList();

	}

	@Override
	public List<Order> findByAuth0Id(String auth0Id) {
		Optional<MemberMongoEntity> existingMember = memberRepository.findByAuth0Id(auth0Id);
		if (existingMember.isEmpty()) {
			LOGGER.error("Member not found: " + auth0Id);
			throw new EntityNotFoundException(auth0Id, "Member");
		}
		UUID memberId = existingMember.get().getId();

		return findByMemberId(memberId);
	}

	@Override
	public List<Order> findMyOrders(String auth0Id, List<String> status, String search) {
		Query query = new Query();

		List<Criteria> criteriaList = new ArrayList<>();

		// Always filter by auth0Id
		criteriaList.add(Criteria.where("orderBy.auth0Id").is(auth0Id));

		// Optional filter: orderStatus
		if (status != null && !status.isEmpty()) {
			criteriaList.add(Criteria.where("orderStatus").in(status));
		}

		// Optional filter: title search
		if (search != null && !search.isBlank()) {
			criteriaList.add(
					Criteria.where("postRef.bookRef.title").regex(search, "i") // case-insensitive
			);
		}

		query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

		List<OrderMongoEntity> entities = mongoTemplate.find(query, OrderMongoEntity.class);

		return entities.stream().map(OrderMongoMapper::toOrder).toList();

	}


	@Override
	public Optional<List<Order>> findAll() {
		List<Order> dtoList = orderRepository.findAll().stream().map(OrderMongoMapper::toOrder).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
