package com.goodmanltd.member.service;

import com.goodmanltd.core.exceptions.NotAuthorizedException;
import com.goodmanltd.core.types.Member;
import com.goodmanltd.core.dto.events.MemberCreatedEvent;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.MemberStatus;
import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.MemberMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.member.dto.CreateMemberRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Profile("mongo")
@Service
public class MemberServiceMongoImpl implements MemberService{

	private final MemberMongoRepository memberRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	public MemberServiceMongoImpl(MemberMongoRepository memberRepository, KafkaTemplate<String, Object> kafkaTemplate) {
		this.memberRepository = memberRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public Member createMember(CreateMemberRequest request) {

		Optional<MemberMongoEntity> existingRecord = memberRepository.findByAuth0Id(request.getAuth0Id());
		if (existingRecord.isPresent()) {
			LOGGER.error("Member already exist: " + request.getAuth0Id());
			// to-do
			// throw exception
			return null;
		}

		// save to mongo
		MemberMongoEntity newMemberEntity = new MemberMongoEntity();
		BeanUtils.copyProperties(request, newMemberEntity);
		newMemberEntity.setId(UUID.randomUUID());
		newMemberEntity.setCreatedAt(LocalDateTime.now());
		newMemberEntity.setReservationCnt(0);
		newMemberEntity.setAnnualTotalReservations(0);
		newMemberEntity.setStatus(MemberStatus.PENDING);

		MemberMongoEntity saved = memberRepository.save(newMemberEntity);

		// kafka
		MemberCreatedEvent memberCreatedEvent = new MemberCreatedEvent();
		BeanUtils.copyProperties(saved, memberCreatedEvent);
		memberCreatedEvent.setId(saved.getId());

		kafkaTemplate.send(KafkaTopics.MEMBER_CREATED, memberCreatedEvent);

		return MemberMongoMapper.toMember(saved);
	}

	@Override
	public Optional<Member> getMemberDetails(String auth0Id) {
		return memberRepository.findByAuth0Id(auth0Id)
				.map(MemberMongoMapper::toMember);

	}

	@Override
	public Optional<List<Member>> getAll() {
		List<Member> dtoList = memberRepository.findAll().stream().map(MemberMongoMapper::toMember).toList();
		return dtoList.isEmpty()? Optional.empty(): Optional.of(dtoList);
	}
}
