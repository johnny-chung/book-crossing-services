package com.goodmanltd.core.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.Message;
import com.goodmanltd.core.dao.mongo.entity.MessageMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class MessageMongoMapper {
	public static Message toMessage(MessageMongoEntity entity) {
		Message message = new Message();

		BeanUtils.copyProperties(entity, message);
		message.setParticipant(MemberReference.from(entity.getParticipant()));
		message.setSender(MemberReference.from(entity.getSender()));
		message.setReceiver(MemberReference.from(entity.getReceiver()));
		
		return message;
	}


	public static MessageMongoEntity toEntity(Message dto) {
		MessageMongoEntity entity = new MessageMongoEntity();

		BeanUtils.copyProperties(dto, entity);
		entity.setSender(MemberReference.from(dto.getSender()));
		entity.setParticipant(MemberReference.from(dto.getParticipant()));
		entity.setReceiver(MemberReference.from(dto.getReceiver()));

		return entity;
	}
}
