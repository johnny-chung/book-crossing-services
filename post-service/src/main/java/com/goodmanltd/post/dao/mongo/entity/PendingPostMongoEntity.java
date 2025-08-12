package com.goodmanltd.post.dao.mongo.entity;

import com.goodmanltd.core.types.OrderStatus;
import com.goodmanltd.core.types.PostStatus;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("mongo")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("posts")
public class PendingPostMongoEntity {

	@Id
	private UUID id;
	private UUID postBy;
	private String isbn;
	private String location;
	private String remarks;
	private LocalDateTime createdAt;
	private PostStatus postStatus;

	@Version
	private Long version;
}
