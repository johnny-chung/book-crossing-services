package com.goodmanltd.order.dto;

import com.goodmanltd.core.types.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOrderResponse {
	private UUID orderId;
	private LocalDateTime completedAt;
	private OrderStatus orderStatus;
}
