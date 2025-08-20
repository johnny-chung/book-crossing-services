package com.goodmanltd.core.types;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReference {
	private UUID id;
	private String title;
	private String thumbnail;
	private String category;
	private String language;

	public static BookReference from(BookReference other) {
		if (other == null) return null;
		return new BookReference(
				other.id,
				other.title,
				other.thumbnail,
				other.category,
				other.language
		);
	}
}
