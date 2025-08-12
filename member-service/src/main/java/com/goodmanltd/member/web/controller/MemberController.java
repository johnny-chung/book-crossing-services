package com.goodmanltd.member.web.controller;

import com.goodmanltd.core.types.Member;
import com.goodmanltd.member.dto.CreateMemberRequest;
import com.goodmanltd.member.dto.CreateMemberResponse;
import com.goodmanltd.member.service.MemberService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/members")
public class MemberController {
	private final MemberService memberService;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}


	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CreateMemberResponse createMember(
			@RequestBody @Valid CreateMemberRequest request)
	{
		Member createdMember = memberService.createMember(request);

		var response = new CreateMemberResponse();
		BeanUtils.copyProperties(createdMember, response);
		return response;
	}

	@GetMapping("/{memberId}")
	public Member getMemberDetails(@PathVariable UUID postId) {
		return memberService.getMemberDetails(postId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
	}

	@GetMapping("/all")
	public List<Member> getAll(){
		return memberService.getAll()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Members not found"));
	}
}
