package com.goodmanltd.core.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.MemberReference;
import com.goodmanltd.core.types.Order;
import com.goodmanltd.core.dao.mongo.entity.OrderMongoEntity;
import com.goodmanltd.core.types.OrderReference;
import com.goodmanltd.core.types.PostReference;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class OrderMongoMapper {
	public static Order toOrder(OrderMongoEntity entity) {
		Order order = new Order();

		BeanUtils.copyProperties(entity, order);
		order.setOrderBy(MemberReference.from(entity.getOrderBy()));
		order.setPostRef(PostReference.from(entity.getPostRef()));
		
		return order;
	}

	public static OrderReference toOrderRef(OrderMongoEntity entity){

		OrderReference orderRef = new OrderReference();
		BeanUtils.copyProperties(entity, orderRef);
		orderRef.setOrderBy(MemberReference.from(entity.getOrderBy()));

		return orderRef;
	}

	public static OrderMongoEntity toEntity(Order dto) {
		OrderMongoEntity entity = new OrderMongoEntity();

		BeanUtils.copyProperties(dto, entity);
		entity.setOrderBy(MemberReference.from(dto.getOrderBy()));
		entity.setPostRef(PostReference.from(dto.getPostRef()));

		return entity;
	}
}
