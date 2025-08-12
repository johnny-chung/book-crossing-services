package com.goodmanltd.core.types;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

	private UUID id;
	private MemberReference postBy;
	private BookReference bookRef;

	private String location;
	private String remarks;
	private LocalDateTime createdAt;

	private OrderReference orderRef;

	private PostStatus postStatus;
}
