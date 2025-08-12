package com.goodmanltd.member.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.Order;
import com.goodmanltd.member.dao.mongo.entity.OrderMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class OrderMongoMapper {
	public static Order toOrder(OrderMongoEntity entity) {
		Order order = new Order();
		BeanUtils.copyProperties(entity, order);
		return order;
	}

	public static OrderMongoEntity toEntity(Order dto) {
		OrderMongoEntity entity = new OrderMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		return entity;
	}
}
