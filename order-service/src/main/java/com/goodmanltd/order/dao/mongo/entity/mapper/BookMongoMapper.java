package com.goodmanltd.order.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.Book;
import com.goodmanltd.order.dao.mongo.entity.BookMongoEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class BookMongoMapper {
	public static Book toBook(BookMongoEntity entity) {
		Book book = new Book();
		BeanUtils.copyProperties(entity, book);
		return book;
	}

	public static BookMongoEntity toEntity(Book dto) {
		BookMongoEntity entity = new BookMongoEntity();
		BeanUtils.copyProperties(dto, entity);
		return entity;
	}
}