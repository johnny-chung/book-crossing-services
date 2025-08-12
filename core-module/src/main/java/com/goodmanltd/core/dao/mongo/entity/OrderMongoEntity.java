package com.goodmanltd.core.dao.mongo.entity;

import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostReference;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("mongo")
@Document("orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMongoEntity {

	@Id
	private UUID id;
	private MemberReference orderBy;
	private PostReference postRef;

	private LocalDateTime createdAt;
	private LocalDateTime completedAt;
	private OrderStatus orderStatus;

	@Version
	private Long version;
}
