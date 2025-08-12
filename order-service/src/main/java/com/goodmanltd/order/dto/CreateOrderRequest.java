package com.goodmanltd.order.dto;

import com.goodmanltd.core.types.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

	@NotNull
	@NotBlank
	private UUID memberId;
	@NotNull
	@NotBlank
	private UUID postId;

}
