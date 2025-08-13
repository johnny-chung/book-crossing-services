package com.goodmanltd.core.dto.events.mapper;

import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dto.events.PostCreatedEvent;
import com.goodmanltd.core.types.BookReference;
import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.Post;
import org.springframework.beans.BeanUtils;

public class PostEventMapper {
	public static Post createdEventToPost(PostCreatedEvent event) {
		Post post = new Post();
		BeanUtils.copyProperties(event, post);
		post.setBookRef(BookReference.from(event.getBookRef()));
		post.setPostBy(MemberReference.from(event.getPostBy()));
		return post;
	}

	public static PostMongoEntity createdEventToEntity(PostCreatedEvent event) {
		PostMongoEntity entity = new PostMongoEntity();
		BeanUtils.copyProperties(event, entity);
		entity.setBookRef(BookReference.from(event.getBookRef()));
		entity.setPostBy(MemberReference.from(event.getPostBy()));
		return entity;
	}
}
