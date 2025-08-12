package com.goodmanltd.order.dao.jpa.entity;

import com.goodmanltd.core.types.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

@Profile("jpa")
@Data
@NoArgsConstructor
@Entity
@Table(name = "books")
public class BookJpaEntity {

	@Id
	private UUID id;

	private String title;
	private String author;
	private String isbn;
	private String category;
	private String language;
	private UUID ownedBy;
	private boolean reserved;
	private UUID reservedBy;
	private UUID orderId;
	private OrderStatus status;

	@Version
	private Long version;
}
