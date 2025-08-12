package com.goodmanltd.order.dao.mongo.entity;

import com.goodmanltd.core.types.OrderStatus;
import jakarta.persistence.*;
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
