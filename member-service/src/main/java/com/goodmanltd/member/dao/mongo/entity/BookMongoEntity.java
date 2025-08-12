package com.goodmanltd.member.dao.mongo.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Profile("mongo")
@Data
@NoArgsConstructor
@Document("books")
public class BookMongoEntity {

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

	@Version
	private Long version;
}
