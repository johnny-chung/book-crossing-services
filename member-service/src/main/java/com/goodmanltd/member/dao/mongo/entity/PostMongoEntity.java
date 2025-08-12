package com.goodmanltd.member.dao.mongo.entity;

import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("mongo")
@Data
@NoArgsConstructor
@Document("posts")
public class PostMongoEntity {
	@Id
	private UUID id;
	private UUID postBy;
	private UUID bookId;
	private String bookTitle;
	private String thumbnail;
	private String location;
	private String remarks;
	private LocalDateTime createdAt;
	private UUID reservedBy;
	private UUID orderId;
	private OrderStatus orderStatus;
	private PostStatus postStatus;


	@Version
	private Long version;
}
