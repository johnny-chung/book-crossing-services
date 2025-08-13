package com.goodmanltd.book.web.controller;


import com.goodmanltd.core.types.Book;
import com.goodmanltd.book.dto.CreateBookRequest;
import com.goodmanltd.book.dto.CreateBookResponse;
import com.goodmanltd.book.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {
	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CreateBookResponse createBook(
			@RequestBody @Valid CreateBookRequest request)
	{
		Book createdBook = bookService.createBook(request);

		var response = new CreateBookResponse();
		BeanUtils.copyProperties(createdBook, response);
		return response;
	}

	@GetMapping("/{bookId}")
	public Book getBookDetails(@PathVariable UUID bookId) {
		return bookService.findByBookId(bookId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
	}
//
//	@GetMapping("/list")
//	public List<Book> getUnreservedList(){
//		return bookService.findUnreservedBook()
//				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Books not found"));
//	}

	@GetMapping("/all")
	public List<Book> getAll(){
		return bookService.findAll()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Books not found"));
	}

	@GetMapping("/health")
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("Book Service is healthy");
	}

}
