package com.goodmanltd.book.service.handler;

import com.goodmanltd.book.dto.CreateBookRequest;
import com.goodmanltd.book.service.BookService;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.core.dto.command.CreateBookCommand;
import com.goodmanltd.core.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("mongo")
@Component
@KafkaListener(topics= KafkaTopics.CREATE_BOOK_COMMAND)
public class CreateBookCommandsMongoHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	//private BookRepository bookRepository;
	private final BookMongoRepository bookRepository;
	private final BookService bookService;

	public CreateBookCommandsMongoHandler(BookMongoRepository bookRepository, BookService bookService) {
		this.bookRepository = bookRepository;
		this.bookService = bookService;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload CreateBookCommand createBookCommand) {
		LOGGER.info("Book service receive create book command: " + createBookCommand.getIsbn());

		CreateBookRequest request = new CreateBookRequest(createBookCommand.getIsbn());

		bookService.createBook(request);

	}
}
