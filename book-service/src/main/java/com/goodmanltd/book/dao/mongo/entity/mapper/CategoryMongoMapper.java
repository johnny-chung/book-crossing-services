package com.goodmanltd.book.dao.mongo.entity.mapper;

import com.goodmanltd.book.dao.mongo.entity.CategoryMongoEntity;
import com.goodmanltd.book.dto.CategoryDto;
import org.springframework.beans.BeanUtils;

public class CategoryMongoMapper {
	public static CategoryDto toCategoryDto(CategoryMongoEntity entity) {
		CategoryDto categoryDto = new CategoryDto();
		BeanUtils.copyProperties(entity, categoryDto);
		return categoryDto;
	}
}
