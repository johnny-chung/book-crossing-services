package com.goodmanltd.core.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.*;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class PostMongoMapper {
	public static Post toPost(PostMongoEntity entity) {
		Post post = new Post();
		BeanUtils.copyProperties(entity, post);
		post.setPostBy(MemberReference.from(entity.getPostBy()));
		post.setOrderRef(OrderReference.from(entity.getOrderRef()));
		post.setBookRef(BookReference.from(entity.getBookRef()));
		return post;
	}
	
	public static PostReference toPostRef(PostMongoEntity entity) {
		PostReference postRef = new PostReference();
		BeanUtils.copyProperties(entity, postRef);
		postRef.setPostBy(MemberReference.from(entity.getPostBy()));
		postRef.setBookRef(BookReference.from(entity.getBookRef()));
		return postRef;
	}



	public static PostMongoEntity toEntity(Post dto) {
		PostMongoEntity entity = new PostMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		entity.setPostBy(MemberReference.from(dto.getPostBy()));
		entity.setOrderRef(OrderReference.from(dto.getOrderRef()));
		entity.setBookRef(BookReference.from(dto.getBookRef()));
		return entity;
	}
}
