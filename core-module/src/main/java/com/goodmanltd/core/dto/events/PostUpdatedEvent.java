package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostUpdatedEvent {
	private UUID id;
	private UUID postBy;
	private UUID bookId;
	private String bookTitle;
	private String location;
	private String remarks;
	private UUID reservedBy;
	private UUID reservedName;
	private UUID orderId;
	private OrderStatus orderStatus;
	private PostStatus postStatus;
}
