package com.goodmanltd.member.dao.mongo.entity;

import com.goodmanltd.core.types.OrderStatus;
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
@Document("orders")
public class OrderMongoEntity {

	@Id
	private UUID id;
	private UUID memberId;
	private UUID postId;
	private LocalDateTime createdAt;
	private LocalDateTime completedAt;
	private OrderStatus orderStatus;

	@Version
	private Long version;
}
