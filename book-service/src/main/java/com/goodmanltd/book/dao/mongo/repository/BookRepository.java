package com.goodmanltd.book.dao.mongo.repository;

import com.goodmanltd.book.dao.mongo.entity.BookEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface BookRepository extends MongoRepository<BookEntity, UUID> {
	Optional<BookEntity> findByIsbn(String isbn);

}