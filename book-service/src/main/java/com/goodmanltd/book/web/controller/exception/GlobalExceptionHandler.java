package com.goodmanltd.book.web.controller.exception;

import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.exceptions.ExternalBookNotFoundException;
import com.goodmanltd.core.exceptions.PostReservedException;
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

	@ExceptionHandler(ExternalBookNotFoundException.class)
	public ResponseEntity<String> handleExternalBookNotFound(ExternalBookNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

}
