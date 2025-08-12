package com.goodmanltd.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleBookVolRes {
	public String id;
	public String selfLink;
	public VolumeInfo volumeInfo;
	public SearchInfo searchInfo;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VolumeInfo {
		public String title;
		public List<String> authors;
		public String mainCategory;
		public String description;
		public String language;
		public ImageLinks imageLinks;

		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class ImageLinks {
			public String thumbnail;
			public String large;
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SearchInfo {
		public String textSnippet;
	}
}