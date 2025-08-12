package com.goodmanltd.core.dao.mongo.repository;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface MemberMongoRepository extends MongoRepository<MemberMongoEntity, UUID> {
	Optional<MemberMongoEntity> findByAuth0Id(String auth0Id);
	Optional<MemberMongoEntity> findById(String Id);
}
