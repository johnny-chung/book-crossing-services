package com.goodmanltd.core.types;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
	private UUID id;
	private UUID postId;

	private MemberReference participant;
	private MemberReference sender;
	private MemberReference receiver;

	private LocalDateTime sentAt;
	private String content;
}
