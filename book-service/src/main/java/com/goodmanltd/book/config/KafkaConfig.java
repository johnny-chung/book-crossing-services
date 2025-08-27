package com.goodmanltd.book.config;

import com.goodmanltd.core.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {


	private final static Integer TOPIC_REPLICATION_FACTOR=3;
	private final static Integer TOPIC_PARTITIONS=3;

	@Autowired
	Environment environment;

	//================================
	// Producer

	Map<String, Object> producerConfigs(){
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				environment.getProperty("spring.kafka.bootstrap-servers"));
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				environment.getProperty("spring.kafka.producer.key-serializer"));
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				environment.getProperty("spring.kafka.producer.value-serializer"));
		config.put(ProducerConfig.ACKS_CONFIG,
				environment.getProperty("spring.kafka.producer.acks"));
		config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
				environment.getProperty("spring.kafka.producer.properties.delivery-timeout-ms"));
		config.put(ProducerConfig.LINGER_MS_CONFIG,
				environment.getProperty("spring.kafka.producer.properties.linger-ms"));
		config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
				environment.getProperty("spring.kafka.producer.properties.request-timeout-ms"));
		config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
				environment.getProperty("spring.kafka.producer.properties.enable.idempotence"));
		config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
				environment.getProperty("spring.kafka.producer.properties.max.in.flight.requests.per.connection"));

		return config;
	}

	@Bean
	ProducerFactory<String, Object> producerFactory(){
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	KafkaTemplate<String, Object> kafkaTemplate(
			ProducerFactory<String, Object> producerFactory
	) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	NewTopic createBookEventsTopic() {
		return TopicBuilder.name(KafkaTopics.BOOK_CREATED)
				.partitions(TOPIC_PARTITIONS)
				.replicas(TOPIC_REPLICATION_FACTOR)
				.build();
	}


}
