package com.goodmanltd.message.dto;

import com.goodmanltd.core.types.BookReference;
import com.goodmanltd.core.types.MemberReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetParticipantListByPost {
	private UUID postId;
	private BookReference bookRef;
	List<MemberReference> participants;
}
