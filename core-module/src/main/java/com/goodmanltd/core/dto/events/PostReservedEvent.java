package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReservedEvent {
	private UUID id;
	private UUID reservedBy;
	private UUID orderId;
	private OrderStatus orderStatus;
	private PostStatus postStatus;
}
