package com.goodmanltd.core.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BookNotFoundException extends RuntimeException {
	private final UUID bookId;

	public BookNotFoundException(UUID bookId) {
		super("Book ("+ bookId + ") not found");
		this.bookId = bookId;
	}
}
