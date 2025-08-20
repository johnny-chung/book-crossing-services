package com.goodmanltd.book.dao.mongo.entity.mapper;

import com.goodmanltd.book.dao.mongo.entity.LanguageMongoEntity;
import com.goodmanltd.book.dto.LanguageDto;
import org.springframework.beans.BeanUtils;

public class LanguageMongoMapper {
	public static LanguageDto toLanguageDto(LanguageMongoEntity entity) {
		LanguageDto languageDto = new LanguageDto();
		BeanUtils.copyProperties(entity, languageDto);
		return languageDto;
	}
}
