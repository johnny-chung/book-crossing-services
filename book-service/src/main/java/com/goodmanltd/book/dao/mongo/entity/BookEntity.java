package com.goodmanltd.book.dao.mongo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Profile("mongo")
@Document("books")
@Data
@NoArgsConstructor
public class BookEntity {

	@Id
	private UUID id;

	private String title;
	private String author;
	private String isbn;
	private String category;
	private String language;
	private String description;
	private String textSnippet;
	private String thumbnail;
	private String imgLarge;

	@Version                               // from org.springframework.data.annotation.Version
	private long version;
}
