package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.MemberReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedEvent {
	private UUID id;
	private UUID postId;
	private MemberReference participant;
	private MemberReference sender;
	private MemberReference receiver;
	private LocalDateTime sentAt;
	private String content;
}
