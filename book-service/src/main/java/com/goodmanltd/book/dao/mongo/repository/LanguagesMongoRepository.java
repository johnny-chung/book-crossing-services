package com.goodmanltd.book.dao.mongo.repository;

import com.goodmanltd.book.dao.mongo.entity.CategoryMongoEntity;
import com.goodmanltd.book.dao.mongo.entity.LanguageMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface LanguagesMongoRepository
		extends MongoRepository<LanguageMongoEntity, UUID>	 {
	Optional<LanguageMongoEntity> findByLanguage(String language);

	@Query("{ 'count' : { $gt: 0 } }")
	List<LanguageMongoEntity> findAllAvailableLanguages();
}