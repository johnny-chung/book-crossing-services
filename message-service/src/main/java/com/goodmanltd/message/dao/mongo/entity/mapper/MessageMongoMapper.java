package com.goodmanltd.message.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.Message;
import com.goodmanltd.message.dao.mongo.entity.MessageMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class MessageMongoMapper {
	public static Message toMessage(MessageMongoEntity entity) {
		Message message = new Message();
		BeanUtils.copyProperties(entity, message);
		return message;
	}

	public static MessageMongoEntity toEntity(Message dto) {
		MessageMongoEntity entity = new MessageMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		return entity;
	}
}
