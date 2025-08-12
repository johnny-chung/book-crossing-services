package com.goodmanltd.core.exceptions;

import java.util.UUID;

public class ReservedLimitReachException extends RuntimeException {
	public ReservedLimitReachException(String memberId) {

      super("Reservation Limit Reached " + memberId);
	}

    public ReservedLimitReachException(UUID memberId) {

      super("Reservation Limit Reached " + memberId);
	}
}
