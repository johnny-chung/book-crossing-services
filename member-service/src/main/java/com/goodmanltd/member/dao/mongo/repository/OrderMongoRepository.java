package com.goodmanltd.member.dao.mongo.repository;

import com.goodmanltd.member.dao.mongo.entity.OrderMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface OrderMongoRepository extends
		MongoRepository<OrderMongoEntity, UUID> {
		List<OrderMongoEntity> findByPostId(UUID postId);
		List<OrderMongoEntity> findByMemberId( UUID memberId);
}
