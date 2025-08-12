package com.goodmanltd.core.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostReference {
	private UUID id;
	private MemberReference postBy;
	private BookReference bookRef;

	public static PostReference from(PostReference other) {
		if (other == null) return null;
		return new PostReference(other.id,
				MemberReference.from(other.postBy),
				BookReference.from(other.bookRef));
	}
}
