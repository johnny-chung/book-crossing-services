package com.goodmanltd.core.dto.events;

import com.goodmanltd.core.types.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCompletedEvent {
	private UUID id;
	private MemberReference postBy;
	private BookReference bookRef;
	private OrderReference orderRef;
	private PostStatus postStatus;
}
