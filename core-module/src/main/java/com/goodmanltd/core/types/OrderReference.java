package com.goodmanltd.core.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReference {
	private UUID id;
	private MemberReference orderBy;

	public static OrderReference from(OrderReference other) {
		if (other == null) return null;
		return new OrderReference(other.id, MemberReference.from(other.orderBy));
	}
}
