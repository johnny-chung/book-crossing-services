package com.goodmanltd.message.dao.mongo.repository;

import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.message.dao.mongo.entity.PostMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface PostMongoRepository extends MongoRepository<PostMongoEntity, UUID> {

	List<PostMongoEntity> findByPostStatus(PostStatus postStatus);
}
