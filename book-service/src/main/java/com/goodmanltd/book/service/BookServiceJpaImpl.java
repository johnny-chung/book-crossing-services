package com.goodmanltd.book.service;

import com.goodmanltd.core.types.Book;
import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.book.dao.jpa.entity.BookEntity;
import com.goodmanltd.book.dao.jpa.entity.mapper.BookMapper;
import com.goodmanltd.book.dao.jpa.repository.BookRepository;
import com.goodmanltd.book.dto.CreateBookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("jpa")
@Service
public class BookServiceJpaImpl implements BookService{

	private final BookRepository bookRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	@Value("${app.kafka.topics.bookCreated}")
	private String bookCreatedTopic;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public BookServiceJpaImpl(BookRepository bookRepository, KafkaTemplate<String, Object> kafkaTemplate) {
		this.bookRepository = bookRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public Book createBook(CreateBookRequest request) {
		// jpa
		BookEntity entity = new BookEntity();

		entity.setTitle("");
		entity.setAuthor("");
		entity.setIsbn(request.getIsbn());
		entity.setCategory("");
		entity.setLanguage("");
//		entity.setOwnedBy(request.getMemberId());
//		entity.setReserved(false);
//		entity.setStatus(OrderStatus.AVAILABLE);


		BookEntity saved = bookRepository.save(entity);

		// kafka
		BookCreatedEvent createNewBook = new BookCreatedEvent(
				saved.getId(),
				saved.getTitle(),
				saved.getAuthor(),
				saved.getIsbn(),
				saved.getLanguage(),
				saved.getCategory(),
				"",
				"",
				"",
				""
		);

		kafkaTemplate.send(bookCreatedTopic, createNewBook);

		LOGGER.info("*** new book id" + saved.getId());

		// return
		return new Book(
				saved.getId(),
				saved.getTitle(),
				saved.getAuthor(),
				saved.getIsbn(),
				saved.getLanguage(),
				saved.getCategory(),
				"",
				"",
				"",
				""
		);
	}

	@Override
	public Optional<Book> findByBookId(UUID bookId) {
		return bookRepository.findById(bookId)
				.map(BookMapper::toBook);
	}
//
//	@Override
//	public Optional<List<Book>> findUnreservedBook() {
//		List<BookEntity> entities = bookRepository.findByReservedFalse();
//		List<Book> dtoList =  entities.stream().map(BookMapper::toBook).toList();
//
//		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
//	}

	@Override
	public Optional<List<Book>> findAll() {
		List<Book> dtoList = bookRepository.findAll().stream().map(BookMapper::toBook).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
