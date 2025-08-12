package com.goodmanltd.message.dao.mongo.repository;


import com.goodmanltd.message.dao.mongo.entity.MessageMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface MessageMongoRepository extends MongoRepository<MessageMongoEntity, UUID> {

	List<MessageMongoEntity> findBySenderId(UUID senderId);
	List<MessageMongoEntity> findByReceiverId(UUID receiverId);

	@Query(value = "{ 'postId': ?0, 'participantId': ?1 }", sort = "{ 'sentAt': -1 }")
	List<MessageMongoEntity> findByPostIdAndParticipantId(UUID postId, UUID participantId);

}
