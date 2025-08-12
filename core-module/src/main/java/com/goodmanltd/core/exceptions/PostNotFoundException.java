package com.goodmanltd.core.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PostNotFoundException extends RuntimeException {
    private final UUID postId;
	public PostNotFoundException(UUID postId) {

        super("Post ("+ postId + ") not found");
		this.postId = postId;
	}
}
