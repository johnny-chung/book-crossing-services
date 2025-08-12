package com.goodmanltd.core.dao.mongo.entity;

import com.goodmanltd.core.types.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("mongo")
@Document("posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostMongoEntity {
	@Id
	private UUID id;
	private MemberReference postBy;
	private BookReference bookRef;

	private String location;
	private String remarks;
	private LocalDateTime createdAt;

	private OrderReference orderRef;

	private PostStatus postStatus;


	@Version
	private Long version;
}
