package com.goodmanltd.core.types;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
	private UUID id;
	private String title;
	private String authors;
	private String isbn;
	private String language;
	private String category;
	private String description;
	private String textSnippet;
	private String thumbnail;
	private String imgLarge;

}
