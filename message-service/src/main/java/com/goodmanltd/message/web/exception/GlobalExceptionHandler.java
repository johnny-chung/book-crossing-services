package com.goodmanltd.message.web.exception;

import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.exceptions.NotAuthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", "Entity not found");
		error.put("entityId", String.valueOf(ex.getId()));
		error.put("entityType", ex.getEntityType());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}


	@ExceptionHandler(NotAuthorizedException.class)
	public ResponseEntity<Map<String, String>> handleNotAuthorized(NotAuthorizedException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", "Not authorized");
		error.put("reason", ex.getMessage()); // Optional: include a custom message from the exception
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error); // 403 Forbidden
	}
}