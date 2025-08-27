package com.goodmanltd.order.dto;

import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResponse {
	private UUID id;
	private OrderStatus orderStatus;
	private PostReference postRef;
}
