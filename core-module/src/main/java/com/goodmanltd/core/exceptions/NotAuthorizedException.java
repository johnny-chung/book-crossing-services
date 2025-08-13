package com.goodmanltd.core.exceptions;

public class NotAuthorizedException extends RuntimeException {
	public NotAuthorizedException() {
		super("Not Authorized");
	}
}
