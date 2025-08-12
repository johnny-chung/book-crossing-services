package com.goodmanltd.core.dao.mongo.repository;

import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface PostMongoRepository extends MongoRepository<PostMongoEntity, UUID> {

	List<PostMongoEntity> findByPostStatus(PostStatus postStatus);

	List<PostMongoEntity> findByPostBy_Id(UUID id);

	List<PostMongoEntity> findByPostBy_Auth0Id(String auth0Id);

	List<PostMongoEntity> findByOrderRef_OrderBy_Id(UUID id);
	List<PostMongoEntity> findByOrderRef_OrderBy_Auth0Id(String auth0Id);
}
