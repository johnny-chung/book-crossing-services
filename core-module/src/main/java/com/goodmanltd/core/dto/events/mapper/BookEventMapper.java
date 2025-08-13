package com.goodmanltd.core.dto.events.mapper;

import com.goodmanltd.core.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.core.dto.events.BookCreatedEvent;
import com.goodmanltd.core.types.Book;
import org.springframework.beans.BeanUtils;

public class BookEventMapper {
	public static Book createdEventToBook(BookCreatedEvent event) {
		Book book = new Book();
		BeanUtils.copyProperties(event, book);
		return book;
	}

	public static BookMongoEntity createdEventToBookEntity(BookCreatedEvent event) {
		BookMongoEntity entity = new BookMongoEntity();
		BeanUtils.copyProperties(event, entity);
		return entity;
	}
}
