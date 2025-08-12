package com.goodmanltd.post.dto;

import com.goodmanltd.core.types.Book;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPostDetailsResponse{

	private UUID id;
	private UUID postBy;
	private UUID bookId;
	private String bookTitle;
	private String thumbnail;
	private String location;
	private String remarks;
	private LocalDateTime createdAt;
	private UUID reservedBy;
	private String reservedName;
	private UUID orderId;
	private OrderStatus orderStatus;
	private PostStatus postStatus;

	private Book bookDetails;

}
