package com.goodmanltd.book.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleBookSearchRes {
	public String kind;
	public List<GoogleBookVolRes> items;
	public Number totalItems;

}
