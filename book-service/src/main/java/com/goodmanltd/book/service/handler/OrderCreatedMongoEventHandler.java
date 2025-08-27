package com.goodmanltd.book.service.handler;

import com.goodmanltd.book.dao.mongo.entity.CategoryMongoEntity;
import com.goodmanltd.book.dao.mongo.entity.LanguageMongoEntity;
import com.goodmanltd.book.dao.mongo.repository.CategoriesMongoRepository;
import com.goodmanltd.book.dao.mongo.repository.LanguagesMongoRepository;
import com.goodmanltd.core.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
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
@KafkaListener(topics= KafkaTopics.ORDER_CREATED)
public class OrderCreatedMongoEventHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private final BookMongoRepository bookRepo;
	private final LanguagesMongoRepository languageRepo;
	private final CategoriesMongoRepository categoryRepo;

	public OrderCreatedMongoEventHandler(BookMongoRepository bookRepo, LanguagesMongoRepository languageRepo, CategoriesMongoRepository categoryRepo) {
		this.bookRepo = bookRepo;
		this.languageRepo = languageRepo;
		this.categoryRepo = categoryRepo;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload OrderCreatedEvent orderCreatedEvent) {
		LOGGER.info("Book service receive order created event: " + orderCreatedEvent.getId());

		// check if book exist
		Optional<BookMongoEntity> existingBook = bookRepo.findById(
				orderCreatedEvent.getPostRef().getBookRef().getId());

		if (existingBook.isEmpty()) {
			LOGGER.error("Book {} not found", orderCreatedEvent.getPostRef().getBookRef().getId());
			throw new RetryableException("Book " + orderCreatedEvent.getPostRef().getBookRef().getId() + " not found");
		}

		// update language
		Optional<LanguageMongoEntity> existingLanguage = languageRepo.findByLanguage(
				existingBook.get().getLanguage());
		LanguageMongoEntity languageToSave = new LanguageMongoEntity();
		if (existingLanguage.isPresent()) {
			BeanUtils.copyProperties(existingLanguage.get(), languageToSave);
			languageToSave.setCount(existingLanguage.get().getCount().intValue() - 1);
			try {
				languageRepo.save(languageToSave);

			} catch (OptimisticLockingFailureException ex) {
				LOGGER.error(ex.getMessage());
				throw new RetryableException(ex);
			}
			catch (DataIntegrityViolationException ex) {
				LOGGER.error(ex.getMessage());
				throw new NotRetryableException(ex);
			}
		}

		// update category
		Optional<CategoryMongoEntity> existingCategory = categoryRepo.findByCategory(
				existingBook.get().getCategory());
		CategoryMongoEntity categoryToSave = new CategoryMongoEntity();
		if (existingCategory.isPresent()) {
			BeanUtils.copyProperties(existingCategory.get(), categoryToSave);
			categoryToSave.setCount(existingCategory.get().getCount().intValue() - 1);
			try {
				categoryRepo.save(categoryToSave);

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
}
