package com.goodmanltd.post.dao.mongo.repository;


import com.goodmanltd.post.dao.mongo.entity.PendingPostMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface PendingPostMongoRepository extends MongoRepository<PendingPostMongoEntity, UUID> {

	List<PendingPostMongoEntity> findByIsbn(String isbn);

}
