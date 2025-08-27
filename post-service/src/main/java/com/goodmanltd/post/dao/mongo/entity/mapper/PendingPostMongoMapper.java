package com.goodmanltd.post.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.Post;
import com.goodmanltd.post.dao.mongo.entity.PendingPostMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class PendingPostMongoMapper {
	public static Post toPost(PendingPostMongoEntity entity) {
		Post post = new Post();
		BeanUtils.copyProperties(entity, post);
		post.setPostBy(MemberReference.from(entity.getPostBy()));
		return post;
	}

	public static PendingPostMongoEntity toEntity(Post dto) {
		PendingPostMongoEntity entity = new PendingPostMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		entity.setPostBy(MemberReference.from(dto.getPostBy()));
		return entity;
	}
}
