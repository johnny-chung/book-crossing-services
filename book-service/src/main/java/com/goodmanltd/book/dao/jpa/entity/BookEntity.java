package com.goodmanltd.book.dao.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@Profile("jpa")
@Data
@NoArgsConstructor
@Entity
@Table(name = "books")
public class BookEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String title;
	private String author;
	private String isbn;
	private String category;
	private String language;

	@Version
	private Long version;
}
