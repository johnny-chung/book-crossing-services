package com.goodmanltd.book.dao.mongo.entity.mapper;

import com.goodmanltd.core.types.Book;
import com.goodmanltd.book.dao.mongo.entity.BookEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;

@Profile("mongo")
public class BookMapper {
	public static Book toBook(BookEntity entity) {
		Book book = new Book();
		BeanUtils.copyProperties(entity, book);
		return book;
	}

	public static BookEntity toEntity(Book dto) {
		BookEntity entity = new BookEntity();
		BeanUtils.copyProperties(dto, entity);
		return entity;
	}
}