package com.goodmanltd.core.kafka;

public class KafkaTopics {
	private KafkaTopics() {}

	public static final String BOOK_CREATED = "book-created-events-topic";
	public static final String CREATE_BOOK_COMMAND = "create-book-commands-topic";

	public static final String MEMBER_CREATED = "member-created-events-topic";
	public static final String MEMBER_UPDATED = "member-updated-events-topic";

	public static final String ORDER_CREATED = "order-created-events-topic";
	public static final String ORDER_PENDING = "order-pending-events-topic";
	public static final String ORDER_COMPLETED = "order-completed-events-topic";

	public static final String POST_CREATED = "post-created-events-topic";
	public static final String POST_UPDATED = "post-updated-events-topic";
	public static final String POST_RESERVED = "post-reserved-events-topic";

	public static final String MESSAGE_CREATED = "message-created-events-topic";

}
