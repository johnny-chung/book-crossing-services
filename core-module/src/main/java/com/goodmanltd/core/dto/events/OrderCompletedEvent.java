package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {
	private UUID id;
	private UUID postId;
	private UUID memberId;
	private OrderStatus orderStatus;
	private LocalDateTime completedAt;
}
