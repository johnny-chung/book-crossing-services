package com.goodmanltd.core.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberReference {
	private UUID id;
	private String auth0Id;
	private String name;

	public static MemberReference from(MemberReference other) {
		if (other == null) return null;
		return new MemberReference(other.id, other.auth0Id, other.name);
	}
}
