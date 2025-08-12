package com.goodmanltd.message.dto;

import com.goodmanltd.core.dao.mongo.entity.MessageMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgGpByPostAndParticipant {
	private UUID participantId;
	private List<MessageMongoEntity> messages;
}
