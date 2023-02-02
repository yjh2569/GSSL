package com.drdoc.BackEnd.api.service;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.drdoc.BackEnd.api.domain.Board;
import com.drdoc.BackEnd.api.domain.BoardType;
import com.drdoc.BackEnd.api.domain.Comment;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.CommentListDto;
import com.drdoc.BackEnd.api.domain.dto.CommentModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.CommentWriteRequestDto;
import com.drdoc.BackEnd.api.repository.BoardRepository;
import com.drdoc.BackEnd.api.repository.CommentRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private BoardRepository boardRepository;
	
	@Mock
	private CommentRepository commentRepository;
	
	@InjectMocks
	private CommentServiceImpl commentService;
	
	private User defaultUser;
	private User anotherUser;
	private BoardType defaultBoardType;
	private Board defaultBoard;
	private Comment defaultComment;
	
	private final String MEMBER_ID = "test";
	private final String BOARD_TITLE = "test";
	private final String BOARD_CONTENT = "testContent";
	private final String BOARD_IMAGE = "testImg";
	
	@BeforeEach
	public void setUp() {
		defaultUser = User.builder()
				.id(1)
				.memberId("test")
				.email("test@test.com")
				.password("password")
				.nickname("testNick")
				.gender("M")
				.phone("01012345678")
				.build();
		anotherUser = User.builder()
				.id(2)
				.memberId("test2")
				.email("test@test.com")
				.password("password")
				.nickname("testNick2")
				.gender("M")
				.phone("01012345678")
				.build();
		defaultBoardType = BoardType.builder()
				.title("T1")
				.build();
		defaultBoard = Board.builder()
				.user(defaultUser)
				.type(defaultBoardType)
				.title(BOARD_TITLE)
				.content(BOARD_CONTENT)
				.image(BOARD_IMAGE)
				.createdTime(LocalDateTime.now())
				.views(10)
				.comments(new ArrayList<>())
				.build();
		defaultComment = Comment.builder()
				.user(defaultUser)
				.board(defaultBoard)
				.content("test")
				.createdTime(LocalDateTime.now())
				.build();
	}
	
	@Test
	@DisplayName("댓글 작성")
	public void writeComment() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(anyInt())).thenReturn(Optional.of(defaultBoard));
		when(commentRepository.save(any())).thenReturn(defaultComment);
		CommentWriteRequestDto requestDto = prepareWrite();
		Comment savedComment = commentService.writeComment(MEMBER_ID, requestDto);
		compareTwoComments(savedComment, defaultComment);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 댓글 작성 실패")
	public void writeCommentNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		CommentWriteRequestDto requestDto = prepareWrite();
		assertThrows(IllegalArgumentException.class, () -> {			
			commentService.writeComment(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 게시글에 댓글 작성 실패")
	public void writeCommentAtBoardNotExist() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(anyInt())).thenReturn(Optional.empty());
		CommentWriteRequestDto requestDto = prepareWrite();
		assertThrows(IllegalArgumentException.class, () -> {			
			commentService.writeComment(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("댓글 수정")
	public void modifyComment() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(commentRepository.findById(anyInt())).thenReturn(Optional.of(defaultComment));
		when(commentRepository.save(any())).thenReturn(defaultComment);
		CommentModifyRequestDto requestDto = prepareModify();
		Comment savedComment = commentService.modifyComment(MEMBER_ID, defaultComment.getId(), requestDto);
		compareTwoComments(savedComment, defaultComment);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 댓글 수정 실패")
	public void modifyCommentNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		CommentModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			commentService.modifyComment(MEMBER_ID, defaultComment.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 댓글 수정 실패")
	public void modifyCommentAtBoardNotExist() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(commentRepository.findById(anyInt())).thenReturn(Optional.empty());
		CommentModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			commentService.modifyComment(MEMBER_ID, defaultComment.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("수정 권한이 없는 댓글 수정 실패")
	public void modifyCommentOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(commentRepository.findById(anyInt())).thenReturn(Optional.of(defaultComment));
		CommentModifyRequestDto requestDto = prepareModify();
		assertThrows(AccessDeniedException.class, () -> {			
			commentService.modifyComment(MEMBER_ID, defaultComment.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("댓글 삭제")
	public void deleteComment() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(commentRepository.findById(anyInt())).thenReturn(Optional.of(defaultComment));
		commentService.deleteComment(MEMBER_ID, defaultComment.getId());
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 댓글 삭제 실패")
	public void deleteCommentNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			commentService.deleteComment(MEMBER_ID, defaultComment.getId());
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 댓글 삭제 실패")
	public void deleteCommentAtBoardNotExist() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(commentRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			commentService.deleteComment(MEMBER_ID, defaultComment.getId());
		});
	}
	
	@Test
	@DisplayName("수정 권한이 없는 댓글 삭제 실패")
	public void deleteCommentOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(commentRepository.findById(anyInt())).thenReturn(Optional.of(defaultComment));
		assertThrows(AccessDeniedException.class, () -> {			
			commentService.deleteComment(MEMBER_ID, defaultComment.getId());
		});
	}
	
	@Test
	@DisplayName("댓글 리스트 조회")
	public void getCommentList() {
		List<Comment> comments = new ArrayList<>();
		Comment comment1 = Comment.builder()
				.user(defaultUser)
				.board(defaultBoard)
				.content("test1")
				.createdTime(LocalDateTime.now())
				.build();
		Comment comment2 = Comment.builder()
				.user(defaultUser)
				.board(defaultBoard)
				.content("test2")
				.createdTime(LocalDateTime.now())
				.build();
		comments.add(defaultComment);
		comments.add(comment1);
		comments.add(comment2);
		when(commentRepository.findByBoardIdOrderByIdDesc(anyInt())).thenReturn(comments);
		List<CommentListDto> commentList = commentService.getCommentList(defaultComment.getId());
		assertEquals(commentList.size(), comments.size());
	}
	
	private CommentWriteRequestDto prepareWrite() {
		return CommentWriteRequestDto.builder()
				.board_id(defaultBoard.getId())
				.content("test")
				.build();
	}
	
	private CommentModifyRequestDto prepareModify() {
		return CommentModifyRequestDto.builder()
				.content("test")
				.build();
	}
	
	private void compareTwoComments(Comment comment1, Comment comment2) {
		assertEquals(comment1.getId(), comment2.getId());
		assertEquals(comment1.getUser().getId(), comment2.getUser().getId());
		assertEquals(comment1.getBoard().getId(), comment2.getBoard().getId());
		assertEquals(comment1.getContent(), comment2.getContent());
		assertEquals(comment1.getCreatedTime(), comment2.getCreatedTime());
	}
}
