package com.goodmanltd.book.service;

import com.goodmanltd.book.dao.mongo.entity.CategoryMongoEntity;
import com.goodmanltd.book.dao.mongo.entity.LanguageMongoEntity;
import com.goodmanltd.book.dao.mongo.entity.mapper.CategoryMongoMapper;
import com.goodmanltd.book.dao.mongo.entity.mapper.LanguageMongoMapper;
import com.goodmanltd.book.dao.mongo.repository.CategoriesMongoRepository;
import com.goodmanltd.book.dao.mongo.repository.LanguagesMongoRepository;
import com.goodmanltd.book.dto.CategoryDto;
import com.goodmanltd.book.dto.CreateBookRequest;
import com.goodmanltd.book.dto.GoogleBookVolRes;
import com.goodmanltd.book.dto.LanguageDto;
import com.goodmanltd.core.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.BookMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.core.exceptions.ExternalBookNotFoundException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
	private final LanguagesMongoRepository languageRepo;
	private final CategoriesMongoRepository categoryRepo;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final GoogleBooksClient googleBooksClient;


	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public BookServiceMongoImpl(BookMongoRepository bookRepository, LanguagesMongoRepository languageRepo, CategoriesMongoRepository categoryRepo, KafkaTemplate<String, Object> kafkaTemplate, GoogleBooksClient googleBooksClient) {
		this.bookRepository = bookRepository;
		this.languageRepo = languageRepo;
		this.categoryRepo = categoryRepo;
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
			LOGGER.info("** googleBook: " + googleBook);
//			entity.setTitle(googleBook.getVolumeInfo().getTitle());
//			entity.setCategory(googleBook.getVolumeInfo().getMainCategory());//
//			entity.setLanguage(googleBook.getVolumeInfo().getLanguage());//
//			entity.setDescription(googleBook.getVolumeInfo().getDescription());//
//			entity.setThumbnail(googleBook.getVolumeInfo().getImageLinks().getThumbnail());//
//			entity.setImgLarge(googleBook.getVolumeInfo().getImageLinks().getLarge());//
//			entity.setTextSnippet(googleBook.getSearchInfo().getTextSnippet());

			List<String> authors = googleBook.getVolumeInfo().getAuthors();
			entity.setAuthors(authors != null ? String.join(", ", authors) : "");


			List<String> categories = googleBook.getVolumeInfo().getCategories();
			entity.setCategory(
					(categories != null && !categories.isEmpty()) ? categories.get(0) : ""
			);
			entity.setTitle(
					Optional.ofNullable(googleBook.getVolumeInfo().getTitle())
							.orElse(""));

			entity.setLanguage(
					Optional.ofNullable(googleBook.getVolumeInfo().getLanguage())
							.orElse("Unknown"));
			entity.setDescription(
					Optional.ofNullable(googleBook.getVolumeInfo().getDescription())
							.orElse(""));
			entity.setThumbnail(
					Optional.ofNullable(googleBook.getVolumeInfo().getImageLinks().getThumbnail())
					.orElse(""));
			entity.setImgLarge(Optional.ofNullable(googleBook.getVolumeInfo().getImageLinks().getLarge())
					.orElse(entity.getThumbnail()));
			entity.setTextSnippet(Optional.ofNullable(googleBook.getSearchInfo().getTextSnippet())
					.orElse(""));

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

		// update language

		Optional<LanguageMongoEntity> existingLanguage = languageRepo.findByLanguage(saved.getLanguage());
		LanguageMongoEntity languageToSave = new LanguageMongoEntity();
		if (existingLanguage.isPresent()) {
			BeanUtils.copyProperties(existingLanguage.get(), languageToSave);
			languageToSave.setCount(existingLanguage.get().getCount().intValue() + 1);
		} else {
			languageToSave.setId(UUID.randomUUID());
			languageToSave.setLanguage(saved.getLanguage());
			languageToSave.setCount(1);
		}
		LOGGER.info("languages: " + languageToSave);
		languageRepo.save(languageToSave);

		// update category
		Optional<CategoryMongoEntity> existingCategory = categoryRepo.findByCategory(saved.getCategory());
		CategoryMongoEntity categoryToSave = new CategoryMongoEntity();
		if (existingCategory.isPresent()) {
			BeanUtils.copyProperties(existingCategory.get(), categoryToSave);
			categoryToSave.setCount(existingCategory.get().getCount().intValue() + 1);
		} else {
			categoryToSave.setId(UUID.randomUUID());
			categoryToSave.setCategory(saved.getCategory());
			categoryToSave.setCount(1);
		}

		LOGGER.info("catetories" + categoryToSave);
		categoryRepo.save(categoryToSave);


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

	@Override
	public List<LanguageDto> findLanguages() {
		return languageRepo.findAllAvailableLanguages()
				.stream().map(LanguageMongoMapper::toLanguageDto).toList();

	}

	@Override
	public List<CategoryDto> findCategories() {
		return categoryRepo.findAllAvailableCategories()
				.stream().map(CategoryMongoMapper::toCategoryDto).toList();
	}
}
