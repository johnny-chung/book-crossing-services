package com.goodmanltd.post.dto;

import com.goodmanltd.core.types.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
	@NotNull
	@NotBlank
	private UUID postBy;
	@NotNull
	@NotBlank
	private String isbn;
	private String location;
	private String remarks;
}
