package com.goodmanltd.post.dto;

import com.goodmanltd.core.types.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPostDetailsResponse{

	private UUID id;
	private MemberReference postBy;
	private BookReference bookRef;

	private String location;
	private String remarks;
	private LocalDateTime createdAt;

	private OrderReference orderRef;

	private PostStatus postStatus;

	private Book bookDetails;

}
