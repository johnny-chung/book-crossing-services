package com.goodmanltd.core.dto.events.mapper;

import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.dto.events.OrderCreatedEvent;
import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.Order;
import com.goodmanltd.core.types.PostReference;
import org.springframework.beans.BeanUtils;

public class OrderEventMapper {
	public static Order createdEventToOrder(OrderCreatedEvent event) {
		Order order = new Order();
		BeanUtils.copyProperties(event, order);
		order.setOrderBy(MemberReference.from(event.getOrderBy()));
		order.setPostRef(PostReference.from(event.getPostRef()));
		return order;
	}

	public static OrderMongoEntity createdEventToEntity(OrderCreatedEvent event) {
		OrderMongoEntity entity = new OrderMongoEntity();
		BeanUtils.copyProperties(event, entity);
		entity.setOrderBy(MemberReference.from(event.getOrderBy()));
		entity.setPostRef(PostReference.from(event.getPostRef()));
		return entity;
	}
}
