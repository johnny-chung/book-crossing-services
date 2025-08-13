package com.goodmanltd.book.service;

import com.goodmanltd.book.dto.GoogleBookVolRes;
import com.goodmanltd.core.types.Book;
import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.core.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.BookMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.book.dto.CreateBookRequest;
import com.goodmanltd.core.exceptions.ExternalBookNotFoundException;
import com.goodmanltd.core.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Service
public class BookServiceMongoImpl implements BookService{

	private final BookMongoRepository bookRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final GoogleBooksClient googleBooksClient;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public BookServiceMongoImpl(BookMongoRepository bookRepository, KafkaTemplate<String, Object> kafkaTemplate, GoogleBooksClient googleBooksClient) {
		this.bookRepository = bookRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.googleBooksClient = googleBooksClient;
	}

	@Override
	public Book createBook(CreateBookRequest request) {

		Optional<BookMongoEntity> existingEntity = bookRepository.findByIsbn(request.getIsbn());
		if (existingEntity.isPresent()) {
			LOGGER.info("book {} already exist", request.getIsbn());
			return BookMongoMapper.toBook(existingEntity.get());
		}

		BookMongoEntity entity = new BookMongoEntity();

		entity.setId(UUID.randomUUID());
		entity.setIsbn(request.getIsbn());

		Optional<GoogleBookVolRes> googleBookInfo = googleBooksClient.fetchByIsbn(request.getIsbn());

		if (googleBookInfo.isPresent()) {
			GoogleBookVolRes googleBook = googleBookInfo.get();

			entity.setTitle(googleBook.getVolumeInfo().getTitle());

			entity.setAuthors(String.join(", ", googleBook.getVolumeInfo().getAuthors()));

			entity.setCategory(googleBook.getVolumeInfo().getMainCategory());

			entity.setLanguage(googleBook.getVolumeInfo().getLanguage());

			entity.setDescription(googleBook.getVolumeInfo().getDescription());

			entity.setThumbnail(googleBook.getVolumeInfo().getImageLinks().getThumbnail());

			entity.setImgLarge(googleBook.getVolumeInfo().getImageLinks().getLarge());

			entity.setTextSnippet(googleBook.getSearchInfo().getTextSnippet());

		} else {
			// throw exception
			throw new ExternalBookNotFoundException(request.getIsbn());
		}


		BookMongoEntity saved = bookRepository.save(entity);

		// kafka
		BookCreatedEvent createNewBook = new BookCreatedEvent(
				saved.getId(),
				saved.getTitle(),
				saved.getAuthors(),
				saved.getIsbn(),
				saved.getLanguage(),
				saved.getCategory(),
				saved.getDescription(),
				saved.getTextSnippet(),
				saved.getThumbnail(),
				saved.getImgLarge()
		);

		kafkaTemplate.send(KafkaTopics.BOOK_CREATED, createNewBook);

		LOGGER.info("*** new book id" + saved.getId());

		// return
		return new Book(
				saved.getId(),
				saved.getTitle(),
				saved.getAuthors(),
				saved.getIsbn(),
				saved.getLanguage(),
				saved.getCategory(),
				saved.getDescription(),
				saved.getTextSnippet(),
				saved.getThumbnail(),
				saved.getImgLarge()
		);
	}

	@Override
	public Optional<Book> findByBookId(UUID bookId) {
		return bookRepository.findById(bookId)
				.map(BookMongoMapper::toBook);
	}

//	@Override
//	public Optional<List<Book>> findUnreservedBook() {
//		List<BookEntity> entities = bookRepository.findByReservedFalse();
//		List<Book> dtoList =  entities.stream().map(BookMapper::toBook).toList();
//
//		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
//	}

	@Override
	public Optional<List<Book>> findAll() {
		List<Book> dtoList = bookRepository.findAll().stream().map(BookMongoMapper::toBook).toList();

		return dtoList.isEmpty() ? Optional.empty() : Optional.of(dtoList);
	}
}
