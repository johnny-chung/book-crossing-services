package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostReference;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
	private UUID id;
	private MemberReference orderBy;
	private PostReference postRef;
	private LocalDateTime createdAt;
	private OrderStatus orderStatus;
}
