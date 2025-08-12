package com.goodmanltd.core.exceptions;

public class ExternalBookNotFoundException extends RuntimeException{
	public ExternalBookNotFoundException(String isbn) {
		super("Book not found in Google Books API for ISBN: " + isbn);
	}
}
