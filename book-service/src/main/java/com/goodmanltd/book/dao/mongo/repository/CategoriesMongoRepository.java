package com.goodmanltd.book.dao.mongo.repository;

import com.goodmanltd.book.dao.mongo.entity.CategoryMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface CategoriesMongoRepository
		extends MongoRepository<CategoryMongoEntity, UUID>	 {

	Optional<CategoryMongoEntity> findByCategory(String category);

	@Query("{ 'count' : { $gt: 0 } }")
	List<CategoryMongoEntity> findAllAvailableCategories();
}