package com.goodmanltd.post.config;

import com.goodmanltd.core.exceptions.NotRetryableException;
import com.goodmanltd.core.exceptions.RetryableException;
import com.goodmanltd.core.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

//	@Value("${app.kafka.topics.postCreated}")
//	private String postCreatedTopic;
	private final static Integer TOPIC_REPLICATION_FACTOR=3;
	private final static Integer TOPIC_PARTITIONS=3;

	@Bean
	KafkaTemplate<String, Object> kafkaTemplate(
			ProducerFactory<String, Object> producerFactory
	) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
			ConsumerFactory<String, Object> comsumerFactory,
			KafkaTemplate<String, Object> kafkaTemplate
	) {
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(
				new DeadLetterPublishingRecoverer(kafkaTemplate),
				new FixedBackOff(5000, 3)
		);

		errorHandler.addNotRetryableExceptions(NotRetryableException.class);
		errorHandler.addRetryableExceptions(RetryableException.class);

		ConcurrentKafkaListenerContainerFactory<String, Object> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(comsumerFactory);
		factory.setCommonErrorHandler(errorHandler);

		return factory;
	}


	@Bean
	NewTopic createPostEventsTopic() {
		return TopicBuilder.name(KafkaTopics.POST_CREATED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic updatePostEventsTopic() {
		return TopicBuilder.name(KafkaTopics.POST_UPDATED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic reservePostEventsTopic() {
		return TopicBuilder.name(KafkaTopics.POST_RESERVED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic completePostEventsTopic() {
		return TopicBuilder.name(KafkaTopics.POST_COMPLETED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic createBookCommentsTopic() {
		return TopicBuilder.name(KafkaTopics.CREATE_BOOK_COMMAND)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
}
