package com.goodmanltd.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberRequest {

	@NotNull
	@NotBlank
	private String auth0Id;

	@NotNull
	@NotBlank
	private String name;

	@NotNull
	@NotBlank
	private String email;
}
