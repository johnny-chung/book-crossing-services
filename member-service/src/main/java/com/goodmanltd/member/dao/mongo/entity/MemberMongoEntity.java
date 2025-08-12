package com.goodmanltd.member.dao.mongo.entity;

import com.goodmanltd.core.types.MemberStatus;
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
@Document("members")
public class MemberMongoEntity {
	@Id
	private UUID id;
	private String auth0Id;
	private String name;
	private String email;
	private LocalDateTime createdAt;
	private MemberStatus status;
	private Number reservedCount;
	private String hashedResetToken;
	private LocalDateTime expiryTime;


	@Version
	private Long version;
}
