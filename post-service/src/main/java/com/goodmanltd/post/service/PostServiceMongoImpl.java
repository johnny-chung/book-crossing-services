package com.goodmanltd.post.service;

import com.goodmanltd.core.dao.mongo.entity.mapper.MemberMongoMapper;
import com.goodmanltd.core.exceptions.NotAuthorizedException;
import com.goodmanltd.core.types.Book;
import com.goodmanltd.core.types.Post;
import com.goodmanltd.core.dto.command.CreateBookCommand;
import com.goodmanltd.core.dto.events.PostCreatedEvent;
import com.goodmanltd.core.exceptions.EntityNotFoundException;
import com.goodmanltd.core.kafka.KafkaTopics;
import com.goodmanltd.core.types.PostStatus;
import com.goodmanltd.core.dao.mongo.entity.BookMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.MemberMongoEntity;
import com.goodmanltd.post.dao.mongo.entity.PendingPostMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.PostMongoEntity;
import com.goodmanltd.core.dao.mongo.entity.mapper.BookMongoMapper;
import com.goodmanltd.post.dao.mongo.entity.mapper.PendingPostMongoMapper;
import com.goodmanltd.core.dao.mongo.entity.mapper.PostMongoMapper;
import com.goodmanltd.core.dao.mongo.repository.BookMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.MemberMongoRepository;
import com.goodmanltd.post.dao.mongo.repository.PendingPostMongoRepository;
import com.goodmanltd.core.dao.mongo.repository.PostMongoRepository;
import com.goodmanltd.post.dto.CreatePostRequest;
import com.goodmanltd.post.dto.GetPostDetailsResponse;
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
public class PostServiceMongoImpl implements PostService{

	private final BookMongoRepository bookRepository;
	private final PostMongoRepository postRepository;
	private final PendingPostMongoRepository pendingPostRepo;
	private final MemberMongoRepository memberRepo;
	private final KafkaTemplate<String, Object> kafkaTemplate;


	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

//	@Value("${app.kafka.topics.postCreated}")
//	private String postCreatedTopic;

	public PostServiceMongoImpl(BookMongoRepository bookRepository, PostMongoRepository postRepository, PendingPostMongoRepository pendingPostRepo, MemberMongoRepository memberRepo, KafkaTemplate<String, Object> kafkaTemplate) {
		this.bookRepository = bookRepository;
		this.postRepository = postRepository;
		this.pendingPostRepo = pendingPostRepo;
		this.memberRepo = memberRepo;
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public Post createPost(CreatePostRequest request) {


		// check member existence
		Optional<MemberMongoEntity> existingMember = memberRepo.findById(request.getPostBy());
		if (existingMember.isEmpty()) {
			LOGGER.error("Member not found: " + request.getPostBy());
			throw new EntityNotFoundException(request.getPostBy(), "Member");
		}

		// check book existence
		Optional<BookMongoEntity> existingBook = bookRepository.findByIsbn(request.getIsbn());
		if (existingBook.isEmpty()) {
			LOGGER.info("book not found when creating post: " + request.getIsbn());

			// kafka
			// issues create book command to book service
			CreateBookCommand createBookCommand = new CreateBookCommand(request.getIsbn());
			kafkaTemplate.send(KafkaTopics.CREATE_BOOK_COMMAND, createBookCommand);

			// save to pending post db
			// to be handled when receive book-created-event
			PendingPostMongoEntity pendingPost = new PendingPostMongoEntity();
			pendingPost.setId(UUID.randomUUID());
			// set isbn for later on update when book created
			pendingPost.setIsbn(request.getIsbn());
			pendingPost.setPostBy(MemberMongoMapper.toMemberRef(existingMember.get()));

			pendingPost.setLocation(request.getLocation());
			pendingPost.setRemarks(request.getRemarks());

			pendingPost.setCreatedAt(LocalDateTime.now());
			pendingPost.setPostStatus(PostStatus.AVAILABLE);

			PendingPostMongoEntity saved = pendingPostRepo.save(pendingPost);
			LOGGER.info("save to pending post " + saved.getId());

			return PendingPostMongoMapper.toPost(saved);
		}

		// save to mongo
		PostMongoEntity newPostEntity = new PostMongoEntity();

		newPostEntity.setId(UUID.randomUUID());
		newPostEntity.setPostBy(MemberMongoMapper.toMemberRef(existingMember.get()));
		newPostEntity.setBookRef(BookMongoMapper.toBookRef(existingBook.get()));
		newPostEntity.setLocation(request.getLocation());
		newPostEntity.setRemarks(request.getRemarks());
		newPostEntity.setCreatedAt(LocalDateTime.now());
		newPostEntity.setPostStatus(PostStatus.AVAILABLE);

		PostMongoEntity saved = postRepository.save(newPostEntity);

		// kafka
		PostCreatedEvent createNewPost = new PostCreatedEvent();
		BeanUtils.copyProperties(saved, createNewPost);

		kafkaTemplate.send(KafkaTopics.POST_CREATED, createNewPost);

		return PostMongoMapper.toPost(saved);
	}

	@Override
	public Optional<GetPostDetailsResponse> findByPostId(UUID postId) {

		GetPostDetailsResponse response = new GetPostDetailsResponse();
		Optional<Post> post = postRepository.findById(postId).map(PostMongoMapper::toPost);

		if (post.isEmpty()) return Optional.empty();

		BeanUtils.copyProperties(post, response);
		Optional<Book> book = bookRepository.findById(post.get().getBookRef().getId()).map(BookMongoMapper::toBook);
		book.ifPresent(response::setBookDetails);

		return Optional.of(response);
	}

	@Override
	public Optional<List<Post>> findByOrderId(UUID orderId) {


		List<PostMongoEntity> postEntityList = postRepository.findByOrderRef_Id(orderId);

		if (postEntityList.isEmpty()) return Optional.empty();

		List<Post> dtoList = postEntityList.stream().map(PostMongoMapper::toPost).toList();
		return dtoList.isEmpty()? Optional.empty(): Optional.of(dtoList);
	}

	@Override
	public Optional<List<Post>> findMemberPost(String auth0Id) {


		List<PostMongoEntity> entities = postRepository.findByPostBy_Auth0Id(auth0Id);
		List<Post> dtoList = entities.stream().map(PostMongoMapper::toPost).toList();
		return dtoList.isEmpty()? Optional.empty(): Optional.of(dtoList);
	}


	@Override
	public Optional<List<Post>> findByAvailable() {
		List<PostMongoEntity> entities = postRepository.findByPostStatus(PostStatus.AVAILABLE);
		List<Post> dtoList = entities.stream().map(PostMongoMapper::toPost).toList();
		return dtoList.isEmpty()? Optional.empty(): Optional.of(dtoList);
	}

	@Override
	public Optional<List<Post>> findAll() {
		List<Post> dtoList = postRepository.findAll().stream().map(PostMongoMapper::toPost).toList();
		return dtoList.isEmpty()? Optional.empty(): Optional.of(dtoList);
	}
}
