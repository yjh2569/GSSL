package com.drdoc.BackEnd.api.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.drdoc.BackEnd.api.domain.Board;
import com.drdoc.BackEnd.api.domain.BoardType;
import com.drdoc.BackEnd.api.domain.Comment;
import com.drdoc.BackEnd.api.domain.User;

@DataJpaTest
public class CommentRepositoryTest {
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private BoardRepository boardRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BoardTypeRepository boardTypeRepository;

	private User defaultUser;
	private BoardType defaultBoardType;
	private Board defaultBoard;
	private Comment defaultComment;
	
	@BeforeEach
	public void setUp() {
		defaultUser = User.builder()
				.memberId("test")
				.email("test@test.com")
				.password("password")
				.nickname("testNick")
				.gender("M")
				.phone("01012345678")
				.build();
		userRepository.save(defaultUser);
		defaultBoardType = BoardType.builder()
				.title("T1")
				.build();
		boardTypeRepository.save(defaultBoardType);
		defaultBoard = Board.builder()
				.user(defaultUser)
				.type(defaultBoardType)
				.title("test")
				.content("testContent")
				.image("testImg")
				.createdTime(LocalDateTime.now())
				.views(10)
				.build();
		boardRepository.save(defaultBoard);
		defaultComment = Comment.builder()
				.user(defaultUser)
				.board(defaultBoard)
				.content("test")
				.createdTime(LocalDateTime.now())
				.build();
		commentRepository.save(defaultComment);
	}
	
	@Test
	@DisplayName("comment 저장")
	public void save() {
		Comment newComment = Comment.builder()
				.user(defaultUser)
				.board(defaultBoard)
				.content("newTest")
				.createdTime(LocalDateTime.now())
				.build();
		Comment savedComment = commentRepository.save(newComment);
		compareTwoComments(savedComment, newComment);
	}
	
	@Test
	@DisplayName("board 별 comment 리스트 조회")
	public void findByBoardIdOrderByIdDesc() {
		Board anotherBoard = Board.builder()
				.user(defaultUser)
				.type(defaultBoardType)
				.title("testA")
				.content("testContentA")
				.image("testImgA")
				.createdTime(LocalDateTime.now())
				.views(15)
				.build();
		boardRepository.saveAndFlush(anotherBoard);
		Comment commentSavedLater = Comment.builder()
				.user(defaultUser)
				.board(defaultBoard)
				.content("testSavedLater")
				.createdTime(LocalDateTime.now())
				.build();
		commentRepository.saveAndFlush(commentSavedLater);
		Comment commentSavedInOtherBoard = Comment.builder()
				.user(defaultUser)
				.board(anotherBoard)
				.content("testSavedInOtherBoard")
				.createdTime(LocalDateTime.now())
				.build();
		commentRepository.saveAndFlush(commentSavedInOtherBoard);
		List<Comment> comments = commentRepository.findByBoardIdOrderByIdDesc(defaultBoard.getId());
		assertEquals(comments.size(), 2);
		compareTwoComments(comments.get(0), commentSavedLater);
		compareTwoComments(comments.get(1), defaultComment);
	}
	
	@Test
	@DisplayName("comment 상세 조회")
	public void findById() {
		Optional<Comment> opComment = commentRepository.findById(defaultComment.getId());
		assertTrue(opComment.isPresent());
		Comment commentFromRepo = opComment.get();
		compareTwoComments(commentFromRepo, defaultComment);
	}
	
	@Test
	@DisplayName("comment 수정")
	public void update() {
		defaultComment.modify("testContentM");
		Comment updatedComment = commentRepository.save(defaultComment);
		compareTwoComments(updatedComment, defaultComment);
	}
	
	@Test
	@DisplayName("comment 삭제")
	public void delete() {
		commentRepository.delete(defaultComment);
		Optional<Comment> opComment = commentRepository.findById(defaultComment.getId());
		assertFalse(opComment.isPresent());
	}

	private void compareTwoComments(Comment comment1, Comment comment2) {
		assertEquals(comment1.getId(), comment2.getId());
		assertEquals(comment1.getUser().getId(), comment2.getUser().getId());
		assertEquals(comment1.getBoard().getId(), comment2.getBoard().getId());
		assertEquals(comment1.getContent(), comment2.getContent());
		assertEquals(comment1.getCreatedTime(), comment2.getCreatedTime());
	}
}
