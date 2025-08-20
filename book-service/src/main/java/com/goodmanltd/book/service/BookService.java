package com.goodmanltd.book.service;

import com.goodmanltd.book.dto.CategoryDto;
import com.goodmanltd.book.dto.LanguageDto;
import com.goodmanltd.core.types.Book;
import com.goodmanltd.book.dto.CreateBookRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookService {
	Book createBook(CreateBookRequest request);

	Optional<Book> findByBookId(UUID bookId);

	Optional<List<Book>> findAll();

	List<LanguageDto> findLanguages();

	List<CategoryDto> findCategories();
}
