package com.goodmanltd.message.dto;

import com.goodmanltd.core.types.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetConversationResponse {
	List<Message> messages;
	Integer page;
	UUID startMsgId;
	UUID nextMsgId;
}
