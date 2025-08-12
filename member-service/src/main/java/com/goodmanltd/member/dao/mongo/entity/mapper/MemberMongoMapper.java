package com.goodmanltd.member.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.Member;
import com.goodmanltd.member.dao.mongo.entity.MemberMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class MemberMongoMapper {
	public static Member toMember(MemberMongoEntity entity) {
		Member member = new Member();
		BeanUtils.copyProperties(entity, member);
		return member;
	}

	public static MemberMongoEntity toEntity(Member dto) {
		MemberMongoEntity entity = new MemberMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		return entity;
	}
}
