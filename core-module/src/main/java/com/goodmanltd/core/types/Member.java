package com.goodmanltd.core.types;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
	private UUID id;
	private String auth0Id;
	private String name;
	private String email;
	private LocalDateTime createdAt;
	private MemberStatus status;

	private Number reservationCnt;
	private Number annualTotalReservations;

	private Number rating;
}
