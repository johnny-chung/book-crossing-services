package com.goodmanltd.order.dao.jpa.repository;

import com.goodmanltd.order.dao.jpa.entity.BookJpaEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Profile("jpa")
@Repository("bookRepository")
public interface BookRepositoryJpa
		extends JpaRepository<BookJpaEntity, UUID> {

}
