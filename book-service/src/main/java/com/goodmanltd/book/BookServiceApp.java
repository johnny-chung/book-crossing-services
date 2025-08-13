package com.goodmanltd.book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.goodmanltd.book", "com.goodmanltd.core"})
@EnableMongoRepositories(basePackages = "com.goodmanltd.core.dao.mongo.repository")
@EntityScan(basePackages = "com.goodmanltd.core.dao.mongo.entity")
public class BookServiceApp {
	public static void main(String[] args) {

		SpringApplication.run(BookServiceApp.class, args);
	}

	@Bean
	RestTemplate getRestTemplate(){
		return new RestTemplate();
	}
}