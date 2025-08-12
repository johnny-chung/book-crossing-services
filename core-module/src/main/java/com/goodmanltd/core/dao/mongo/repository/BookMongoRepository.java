package com.goodmanltd.core.dao.mongo.repository;

import com.goodmanltd.core.dao.mongo.entity.BookMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface BookMongoRepository
		extends MongoRepository<BookMongoEntity, UUID>	 {
			 Optional<BookMongoEntity> findById(String Id);
			 Optional<BookMongoEntity> findByIsbn(String Isbn);
}