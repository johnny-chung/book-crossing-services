package com.goodmanltd.order.web.controller;

import com.goodmanltd.core.types.Order;
import com.goodmanltd.order.dto.CompleteOrderRequest;
import com.goodmanltd.order.dto.CompleteOrderResponse;
import com.goodmanltd.order.dto.CreateOrderRequest;
import com.goodmanltd.order.dto.CreateOrderResponse;
import com.goodmanltd.order.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderService orderService;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}


	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CreateOrderResponse createOrder(
			@RequestBody @Valid CreateOrderRequest request)
	{
		Order createdOrder = orderService.createOrder(request);

		var response = new CreateOrderResponse();
		BeanUtils.copyProperties(createdOrder, response);
		response.setOrderId(createdOrder.getId());
		return response;
	}

	@GetMapping("/my-orders")
	@PreAuthorize("isAuthenticated()")
	public List<Order> getOrdersByAuthenticatedUser(@AuthenticationPrincipal Jwt jwt) {
		String auth0Id = jwt.getClaimAsString("sub"); // Or use custom claim if needed

		LOGGER.info("Fetching orders for Auth0 ID: {}", auth0Id);

		return orderService.findByAuth0Id(auth0Id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No orders found for this user"));
	}


	@GetMapping("/{orderId}")
	public Order getOrderDetails(@PathVariable UUID orderId) {
		return orderService.findByOrderId(orderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
	}

	@PutMapping("/{orderId}/completed")
	public CompleteOrderResponse setOrderCompleted(@RequestBody @Valid CompleteOrderRequest request) {
		Order completedOrder =  orderService.completeOrder(request);
		var response = new CompleteOrderResponse();
		BeanUtils.copyProperties(completedOrder, response);
		return response;
	}

	@GetMapping("/all")
	public List<Order> getAll(){
		return orderService.findAll()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orders not found"));
	}
}
