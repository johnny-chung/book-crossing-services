package com.goodmanltd.core.dao.mongo.entity;

import com.goodmanltd.core.types.MemberStatus;
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
@Document("members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberMongoEntity {

	@Id
	private UUID id;
	private String auth0Id;
	private String name;
	private String email;
	private LocalDateTime createdAt;
	private MemberStatus status;

	private Number reservationCnt;
	private Number annualTotalReservations;

	private Number rating;

	private String hashedResetToken;
	private LocalDateTime expiryTime;


	@Version
	private Long version;
}
