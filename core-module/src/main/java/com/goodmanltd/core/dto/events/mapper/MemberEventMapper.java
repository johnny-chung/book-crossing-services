package com.goodmanltd.core.dto.events.mapper;

import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dto.events.MemberCreatedEvent;
import com.goodmanltd.core.types.Member;
import org.springframework.beans.BeanUtils;

public class MemberEventMapper {
	public static Member createdEventToMember(MemberCreatedEvent event) {
		Member member = new Member();
		BeanUtils.copyProperties(event, member);
		return member;
	}

	public static MemberMongoEntity createdEventToEntity(MemberCreatedEvent event) {
		MemberMongoEntity entity = new MemberMongoEntity();
		BeanUtils.copyProperties(event, entity);
		return entity;
	}
}
