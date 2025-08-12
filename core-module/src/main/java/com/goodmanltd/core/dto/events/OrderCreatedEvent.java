package com.goodmanltd.core.dto.events;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
	private UUID orderId;
	private UUID postId;
	private UUID memberId;
	private LocalDateTime createdAt;
}
