package com.goodmanltd.order.service;

import com.goodmanltd.core.types.Order;
import com.goodmanltd.order.dto.CancelOrderRequest;
import com.goodmanltd.order.dto.CompleteOrderRequest;
import com.goodmanltd.order.dto.CreateOrderRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
	Order createOrder (CreateOrderRequest request);

	Order completeOrder (CompleteOrderRequest request, String auth0Id);

	Order cancelOrder (CancelOrderRequest request, String auth0Id);

	Optional<Order> findByOrderId(UUID orderId);
	Optional<List<Order>> findByPostId(UUID postId);

	Optional<List<Order>> findByMemberId(UUID memberId);

	Optional<List<Order>> findByAuth0Id(String auth0Id);

	Optional<List<Order>> findAll();

}
