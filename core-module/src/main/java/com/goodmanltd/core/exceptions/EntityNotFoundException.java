package com.goodmanltd.core.exceptions;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
public class EntityNotFoundException extends RuntimeException {
	private final String id;
	private String entityType;
	public EntityNotFoundException(UUID uuid, String entityType) {
		super( entityType + " (" +  uuid.toString() +  ") not found");
		this.id = uuid.toString();
		this.entityType = entityType;
	}
	public EntityNotFoundException(String id, String entityType) {
		super( entityType + " (" +  id +  ") not found");
		this.id = id;
		this.entityType = entityType;
	}
}
