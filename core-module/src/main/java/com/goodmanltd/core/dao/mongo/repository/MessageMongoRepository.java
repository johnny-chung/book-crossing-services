package com.goodmanltd.core.dao.mongo.repository;


import com.goodmanltd.core.dao.mongo.entity.MessageMongoEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Profile("mongo")
@Repository
public interface MessageMongoRepository extends MongoRepository<MessageMongoEntity, UUID> {

	List<MessageMongoEntity> findBySender_Id(UUID senderId);
	List<MessageMongoEntity> findBySender_Auth0Id(String senderAuth0Id);
	List<MessageMongoEntity> findByReceiver_Id(UUID receiverId);
	List<MessageMongoEntity> findByReceiver_Auth0Id(String receiverAuth0Id);

	@Query(value = "{ 'postId': ?0, 'participantId': ?1 }", sort = "{ 'sentAt': -1 }")
	List<MessageMongoEntity> findByPostIdAndParticipantId(UUID postId, UUID participantId);
	List<MessageMongoEntity> findByPostId(UUID postId);

}
