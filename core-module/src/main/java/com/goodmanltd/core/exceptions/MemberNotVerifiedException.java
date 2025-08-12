package com.goodmanltd.core.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MemberNotVerifiedException extends RuntimeException {
	private final UUID memberId;
	public MemberNotVerifiedException(UUID memberId) {

		super("Member " + memberId +" not verified");
		this.memberId = memberId;
	}
}
