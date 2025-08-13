package com.goodmanltd.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(
		scanBasePackages = {"com.goodmanltd.member", "com.goodmanltd.core"}
)
@EnableMongoRepositories(
		basePackages = {"com.goodmanltd.core.dao.mongo.repository"}
)
@EntityScan(
		basePackages = {"com.goodmanltd.core.dao.mongo.entity"}
)
public class MemberServiceApp {
	public static void main(String[] args) {

		SpringApplication.run(MemberServiceApp.class, args);
	}
}