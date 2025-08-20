package com.goodmanltd.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDto {
	private UUID id;
	private String language;
	private Number count;
}
