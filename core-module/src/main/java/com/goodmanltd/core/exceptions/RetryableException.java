package com.goodmanltd.core.exceptions;

public class RetryableException extends RuntimeException {

  // for custom error message
  public RetryableException(String message) {
    super(message);
  }

  // accept original exception
  // pass exception to parent class
  public RetryableException(Throwable cause) {
    super(cause);
  }
}
