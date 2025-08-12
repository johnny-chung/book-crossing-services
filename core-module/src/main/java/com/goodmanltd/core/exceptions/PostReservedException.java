package com.goodmanltd.core.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PostReservedException extends RuntimeException {
  private final UUID postId;
	public PostReservedException(UUID postId) {
      super("Post ("+ postId + ") has been reserved");
      this.postId = postId;
	}
}
