package com.goodmanltd.order.config;

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

	@Value("${app.kafka.topics.orderCreated}")
	private String orderCreatedTopic;
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
			ConsumerFactory<String, Object> consumerFactory,
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

		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);

		return factory;
	}


	@Bean
	NewTopic createOrderEventsTopic() {
		return TopicBuilder.name(KafkaTopics.ORDER_CREATED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic orderPendingEventsTopic() {
		return TopicBuilder.name(KafkaTopics.ORDER_PENDING)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic completeOrderEventsTopic() {
		return TopicBuilder.name(KafkaTopics.ORDER_COMPLETED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}
	@Bean
	NewTopic cancelOrderEventsTopic() {
		return TopicBuilder.name(KafkaTopics.ORDER_CANCELLED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}


}
