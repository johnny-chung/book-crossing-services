package com.goodmanltd.core.dto.events;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCreatedEvent {
	private UUID bookId;
	private String title;
	private String author;
	private String isbn;
	private String language;
	private String category;
	private String description;
	private String textSnippet;
	private String thumbnail;
	private String imgLarge;
}
