package com.goodmanltd.order.service;

import com.goodmanltd.core.types.Order;
import com.goodmanltd.order.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
	CreateOrderResponse createOrder (CreateOrderRequest request);

	CompleteOrderResponse completeOrder (CompleteOrderRequest request, String auth0Id);

	CancelOrderResponse cancelOrder (CancelOrderRequest request, String auth0Id);

	Optional<Order> findByOrderId(UUID orderId);
	Optional<List<Order>> findByPostId(UUID postId);

	List<Order> findByMemberId(UUID memberId);

	List<Order> findByAuth0Id(String auth0Id);

	List<Order> findMyOrders (String auth0Id, List<String> status, String search);

	Optional<List<Order>> findAll();

}
