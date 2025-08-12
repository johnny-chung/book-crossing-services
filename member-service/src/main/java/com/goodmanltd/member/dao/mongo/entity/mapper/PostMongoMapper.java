package com.goodmanltd.member.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.Post;
import com.goodmanltd.member.dao.mongo.entity.PostMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class PostMongoMapper {
	public static Post toOrder(PostMongoEntity entity) {
		Post post = new Post();
		BeanUtils.copyProperties(entity, post);
		return post;
	}

	public static PostMongoEntity toEntity(Post dto) {
		PostMongoEntity entity = new PostMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		return entity;
	}
}
