package com.goodmanltd.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(
		scanBasePackages = {"com.goodmanltd.post", "com.goodmanltd.core"}
)
@EnableMongoRepositories(
		basePackages = {"com.goodmanltd.core.dao.mongo.repository",
				"com.goodmanltd.post.dao.mongo.repository"}
)
@EntityScan(
		basePackages = {"com.goodmanltd.core.dao.mongo.entity",
				"com.goodmanltd.post.dao.mongo.entity"}
)
public class PostServiceApp {
	public static void main(String[] args) {

		SpringApplication.run(PostServiceApp.class, args);
	}
}