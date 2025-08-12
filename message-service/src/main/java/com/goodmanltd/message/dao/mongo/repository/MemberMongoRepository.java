package com.goodmanltd.message.dao.mongo.repository;


import com.goodmanltd.message.dao.mongo.entity.MemberMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Profile("mongo")
@Repository
public interface MemberMongoRepository
		extends MongoRepository<MemberMongoEntity, UUID>
		 {

}