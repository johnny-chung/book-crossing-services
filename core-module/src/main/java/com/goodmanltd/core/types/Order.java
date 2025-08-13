package com.goodmanltd.core.types;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	private UUID id;
	private MemberReference orderBy;
	private PostReference postRef;
	private LocalDateTime createdAt;
	private LocalDateTime completedAt;
	private OrderStatus orderStatus;
}
