package com.goodmanltd.member.service.handler;

import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.member.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.member.dao.mongo.repository.BookMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Profile("mongo")
@Component
@KafkaListener(topics= KafkaTopics.BOOK_CREATED)
public class BookCreatedMongoEventHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	//private BookRepository bookRepository;
	private final BookMongoRepository bookRepository;

	public BookCreatedMongoEventHandler(BookMongoRepository bookRepository) {
		this.bookRepository = bookRepository;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload BookCreatedEvent bookCreatedEvent) {
		LOGGER.info("Member service receive new book created event: " + bookCreatedEvent.getTitle());

		Optional<BookMongoEntity> existingRecord = bookRepository.findById(bookCreatedEvent.getBookId());

		if (existingRecord.isPresent()) {
			LOGGER.info("Found a duplicate book id: {}", existingRecord.get().getId());
			return;
		}

		Optional<BookMongoEntity> existingBook = bookRepository.findByIsbn(bookCreatedEvent.getIsbn());
		if (existingBook.isPresent()) {
			LOGGER.info("Found a book with same ISBN: {}", existingRecord.get().getIsbn());
			return;
		}

		BookMongoEntity entity = new BookMongoEntity();
		BeanUtils.copyProperties(bookCreatedEvent, entity);
		entity.setId(bookCreatedEvent.getBookId());

		try {
			bookRepository.save(entity);

		} catch (OptimisticLockingFailureException ex) {
			LOGGER.error(ex.getMessage());
			throw new RetryableException(ex);
		}
		catch (DataIntegrityViolationException ex) {
			LOGGER.error(ex.getMessage());
			throw new NotRetryableException(ex);
		}

	}
}
