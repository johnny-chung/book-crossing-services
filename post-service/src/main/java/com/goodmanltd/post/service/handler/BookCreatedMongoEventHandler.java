package com.goodmanltd.post.service.handler;

import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.post.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.post.dao.mongo.entity.PendingPostMongoEntity;
import com.goodmanltd.post.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.post.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.post.dao.mongo.repository.PendingPostMongoRepository;
import com.goodmanltd.post.dao.mongo.repository.PostMongoRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Component
@KafkaListener(topics= KafkaTopics.BOOK_CREATED)
public class BookCreatedMongoEventHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final BookMongoRepository bookRepository;
	private final PendingPostMongoRepository pendingPostRepo;
	private final PostMongoRepository postRepo;

	public BookCreatedMongoEventHandler(BookMongoRepository bookRepository, PendingPostMongoRepository pendingPostRepo, PostMongoRepository postRepo) {

		this.bookRepository = bookRepository;
		this.pendingPostRepo = pendingPostRepo;
		this.postRepo = postRepo;
	}


	@Transactional
	@KafkaHandler
	public void handle(@Payload BookCreatedEvent bookCreatedEvent) {
		LOGGER.info("Post service receive new book created event: " + bookCreatedEvent.getTitle());

		Optional<BookMongoEntity> existingRecord = bookRepository.findById(bookCreatedEvent.getBookId());

		if (existingRecord.isPresent()) {
			LOGGER.info("Found a duplicate book id: {}", existingRecord.get().getId());
			return;
		}

		Optional<BookMongoEntity> existingBook = bookRepository.findByIsbn(bookCreatedEvent.getIsbn());
		if (existingBook.isPresent()) {
			LOGGER.info("Found a book with same ISBN: {}", existingBook.get().getIsbn());
			return;
		}

		// save the book
		BookMongoEntity entityToSave = new BookMongoEntity();
		BeanUtils.copyProperties(bookCreatedEvent, entityToSave);
		entityToSave.setId(bookCreatedEvent.getBookId());

		try {
			bookRepository.save(entityToSave);

		} catch (OptimisticLockingFailureException ex) {
			LOGGER.error(ex.getMessage());
			throw new RetryableException(ex);
		}
		catch (DataIntegrityViolationException ex) {
			LOGGER.error(ex.getMessage());
			throw new NotRetryableException(ex);
		}


		// process pending post
		List<PendingPostMongoEntity> pendingPosts = pendingPostRepo.findByIsbn(bookCreatedEvent.getIsbn());

		List<PostMongoEntity> postsToSave = new ArrayList<>();

		for (PendingPostMongoEntity pending : pendingPosts) {
			PostMongoEntity post = new PostMongoEntity();
			post.setId(UUID.randomUUID());
			post.setPostBy(pending.getPostBy());
			post.setBookId(bookCreatedEvent.getBookId());
			post.setBookTitle(bookCreatedEvent.getTitle());
			post.setThumbnail(bookCreatedEvent.getThumbnail());
			post.setLocation(pending.getLocation());
			post.setRemarks(pending.getRemarks());
			post.setCreatedAt(pending.getCreatedAt());
			post.setPostStatus(PostStatus.AVAILABLE);

			postsToSave.add(post);
		}

		postRepo.saveAll(postsToSave); // batch save
		pendingPostRepo.deleteAll(pendingPosts); // clean up

	}
}
