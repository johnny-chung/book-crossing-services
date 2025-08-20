package com.goodmanltd.member.service;

import com.goodmanltd.core.types.Member;
import com.goodmanltd.member.dto.CreateMemberRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface MemberService {
	Member createMember(CreateMemberRequest request);

	Optional<Member> getMemberDetails(String auth0Id);

	Optional<List<Member>> getAll();
}
