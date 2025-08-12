package com.goodmanltd.book.dao.jpa.repository;

import com.goodmanltd.book.dao.jpa.entity.BookEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Profile("jpa")
@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID> {

}

