package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdatedEvent {
	private UUID id;
	private String auth0Id;
	private String name;
	private String email;
	private Number rating;
	private LocalDateTime createdAt;
	private Number reservationCnt;
	private Number annualTotalReservations;
	private MemberStatus status;
}
