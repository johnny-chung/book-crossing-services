package com.goodmanltd.message.dao.mongo.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;
@Profile("mongo")
@Data
@NoArgsConstructor
@CompoundIndex(name = "post_participant_idx", def = "{'postId': 1, 'participantId': 1}")
@Document("messages")
public class MessageMongoEntity {
	@Id
	private UUID id;
	private UUID senderId;
	private UUID receiverId;
	private UUID postId;
	private UUID participantId;
	private String participantName;
	private LocalDateTime sentAt;
	private String content;

	@Version
	private Long version;
}
