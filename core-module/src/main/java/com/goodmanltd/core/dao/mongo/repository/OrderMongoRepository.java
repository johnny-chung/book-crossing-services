package com.goodmanltd.core.dao.mongo.repository;

import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface OrderMongoRepository extends
		MongoRepository<OrderMongoEntity, UUID> {

		List<OrderMongoEntity> findByPostRef_Id(UUID postId);

		List<OrderMongoEntity> findByPostRef_PostBy_Id(UUID postById);

		List<OrderMongoEntity> findByOrderBy_Id(UUID orderById);

		List<OrderMongoEntity> findByPostRef_PostBy_Auth0Id(UUID postById);
		List<OrderMongoEntity> findByOrderBy_Auth0Id(UUID orderById);
}
