package com.goodmanltd.order.dto;

import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
	private UUID id;
	private MemberReference orderBy;
	private PostReference postRef;
	private LocalDateTime createdAt;
	private OrderStatus orderStatus;
}

