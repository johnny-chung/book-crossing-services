package com.goodmanltd.core.dao.mongo.entity;

import com.goodmanltd.core.types.MemberReference;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Profile("mongo")
@Document("messages")
@CompoundIndex(name = "post_participant_idx_v2", def = "{'postId': 1, 'participant.id': 1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageMongoEntity {

	@Id
	private UUID id;
	private UUID postId;

	private MemberReference participant;
	private MemberReference sender;
	private MemberReference receiver;

	private LocalDateTime sentAt;
	private String content;

	@Version
	private Long version;
}
