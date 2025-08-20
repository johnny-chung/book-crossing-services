package com.goodmanltd.book.dao.mongo.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Profile("mongo")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("languages")
public class LanguageMongoEntity {

	@Id
	private UUID id;
	private String language;
	private Number count;

	@Version
	private Long version;
}
