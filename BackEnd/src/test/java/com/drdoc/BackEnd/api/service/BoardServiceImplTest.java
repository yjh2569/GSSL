package com.drdoc.BackEnd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;

import com.drdoc.BackEnd.api.domain.Board;
import com.drdoc.BackEnd.api.domain.BoardType;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.BoardDetailDto;
import com.drdoc.BackEnd.api.domain.dto.BoardListDto;
import com.drdoc.BackEnd.api.domain.dto.BoardModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.BoardWriteRequestDto;
import com.drdoc.BackEnd.api.repository.BoardRepository;
import com.drdoc.BackEnd.api.repository.BoardTypeRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class BoardServiceImplTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private BoardTypeRepository boardTypeRepository;
	
	@Mock
	private BoardRepository boardRepository;
	
	@InjectMocks
	private BoardServiceImpl boardService;
	
	private User defaultUser;
	private User anotherUser;
	private BoardType boardType1;
	private BoardType boardType2;
	private Board defaultBoard;
	
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
		boardType1 = BoardType.builder()
				.title("T1")
				.build();
		boardType2 = BoardType.builder()
				.title("T2")
				.build();
		defaultBoard = Board.builder()
				.user(defaultUser)
				.type(boardType1)
				.title(BOARD_TITLE)
				.content(BOARD_CONTENT)
				.image(BOARD_IMAGE)
				.createdTime(LocalDateTime.now())
				.views(10)
				.comments(new ArrayList<>())
				.build();
	}
	
	@Test
	@DisplayName("게시글 작성")
	public void writeBoard() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardTypeRepository.findById(any())).thenReturn(Optional.of(boardType1));
		when(boardRepository.save(any())).thenReturn(defaultBoard);
		BoardWriteRequestDto requestDto = prepareWrite();
		Board savedBoard = boardService.writeBoard(MEMBER_ID, requestDto);
		compareTwoBoards(savedBoard, defaultBoard);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 게시글 작성 실패")
	public void writeBoardNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		BoardWriteRequestDto requestDto = prepareWrite();
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.writeBoard(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 유형으로 게시글 작성 실패")
	public void writeBoardNotExistBoardType() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardTypeRepository.findById(any())).thenReturn(Optional.empty());
		BoardWriteRequestDto requestDto = prepareWrite();
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.writeBoard(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("게시글 수정")
	public void modifyBoard() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(any())).thenReturn(Optional.of(defaultBoard));
		when(boardTypeRepository.findById(any())).thenReturn(Optional.of(boardType1));
		when(boardRepository.save(any())).thenReturn(defaultBoard);
		BoardModifyRequestDto requestDto = prepareModify();
		Board modifiedBoard = boardService.modifyBoard(MEMBER_ID, defaultBoard.getId(), requestDto);
		compareTwoBoards(modifiedBoard, defaultBoard);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 게시글 수정 실패")
	public void modifyBoardNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		BoardModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.modifyBoard(MEMBER_ID, defaultBoard.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 게시글 수정 실패")
	public void modifyBoardNotExistBoard() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(any())).thenReturn(Optional.empty());
		BoardModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.modifyBoard(MEMBER_ID, defaultBoard.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 유형으로 게시글 수정 실패")
	public void modifyBoardNotExistBoardType() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(any())).thenReturn(Optional.of(defaultBoard));
		when(boardTypeRepository.findById(any())).thenReturn(Optional.empty());
		BoardModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.modifyBoard(MEMBER_ID, defaultBoard.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("수정 권한이 없는 게시글 수정 실패")
	public void modifyBoardOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(boardRepository.findById(any())).thenReturn(Optional.of(defaultBoard));
		when(boardTypeRepository.findById(any())).thenReturn(Optional.of(boardType1));
		BoardModifyRequestDto requestDto = prepareModify();
		assertThrows(AccessDeniedException.class, () -> {			
			boardService.modifyBoard(MEMBER_ID, defaultBoard.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("게시글 이미지 경로 조회")
	public void getBoardImage() {
		when(boardRepository.findById(any())).thenReturn(Optional.of(defaultBoard));
		String image = boardService.getBoardImage(defaultBoard.getId());
		assertEquals(image, defaultBoard.getImage());
	}
	
	@Test
	@DisplayName("존재하지 않는 게시글 이미지 경로 조회 실패")
	public void getNotExistBoardImage() {
		when(boardRepository.findById(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			boardService.getBoardImage(defaultBoard.getId());
		});
	}
	
	@Test
	@DisplayName("게시글 삭제")
	public void deleteBoard() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(any())).thenReturn(Optional.of(defaultBoard));
		boardService.deleteBoard(MEMBER_ID, defaultBoard.getId());
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 게시글 삭제 실패")
	public void deleteBoardNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.deleteBoard(MEMBER_ID, defaultBoard.getId());
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 게시글 삭제 실패")
	public void deleteBoardNotExistBoard() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(boardRepository.findById(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			boardService.deleteBoard(MEMBER_ID, defaultBoard.getId());
		});
	}
	
	@Test
	@DisplayName("삭제 권한이 없는 게시글 삭제 실패")
	public void deleteBoardOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(boardRepository.findById(any())).thenReturn(Optional.of(defaultBoard));
		assertThrows(AccessDeniedException.class, () -> {			
			boardService.deleteBoard(MEMBER_ID, defaultBoard.getId());
		});
	}
	
	@Test
	@DisplayName("게시글 리스트 조회")
	public void getBoardList() {
		List<Board> boardList = new ArrayList<>();
		Board board1 = Board.builder()
				.user(defaultUser)
				.type(boardType1)
				.title(BOARD_TITLE)
				.content(BOARD_CONTENT)
				.image(BOARD_IMAGE)
				.createdTime(LocalDateTime.now())
				.views(10)
				.comments(new ArrayList<>())
				.build();
		Board board2 = Board.builder()
				.user(defaultUser)
				.type(boardType1)
				.title(BOARD_TITLE)
				.content(BOARD_CONTENT)
				.image(BOARD_IMAGE)
				.createdTime(LocalDateTime.now())
				.views(10)
				.comments(new ArrayList<>())
				.build();
		boardList.add(defaultBoard);
		boardList.add(board1);
		boardList.add(board2);
		Page<Board> boards = new PageImpl<>(boardList);
		when(boardRepository.findByTypeIdAndTitleContains(anyInt(), any(), any())).thenReturn(boards);
		Page<BoardListDto> pageOfBoard = boardService.getBoardList(boardType1.getId(), "test", 0, 5);
		List<BoardListDto> listOfBoard = pageOfBoard.getContent();
		assertEquals(listOfBoard.size(), 3);
	}
	
	@Test
	@DisplayName("게시글 상세 조회")
	public void getBoardDetail() {
		when(boardRepository.findById(anyInt())).thenReturn(Optional.of(defaultBoard));
		BoardDetailDto detail = boardService.getBoardDetail(defaultBoard.getId());
		assertEquals(detail.getId(), defaultBoard.getId());
		assertEquals(detail.getTitle(), defaultBoard.getTitle());
		assertEquals(detail.getContent(), defaultBoard.getContent());
	}
	
	@Test
	@DisplayName("존재하지 않는 게시글 상세 조회 실패")
	public void getBoardDetailNotExist() {
		when(boardRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			boardService.getBoardDetail(defaultBoard.getId());			
		});
	}

	private BoardWriteRequestDto prepareWrite() {
		return BoardWriteRequestDto.builder()
				.type_id(boardType1.getId())
				.title("title")
				.content("content")
				.image("image")
				.build();
	}
	
	private BoardModifyRequestDto prepareModify() {
		return BoardModifyRequestDto.builder()
				.type_id(boardType2.getId())
				.title("title2")
				.content("content2")
				.image("image2")
				.build();
	}

	private void compareTwoBoards(Board board1, Board board2) {
		assertEquals(board1.getId(), board2.getId());
		assertEquals(board1.getUser().getId(), board2.getUser().getId());
		assertEquals(board1.getType().getId(), board2.getType().getId());
		assertEquals(board1.getTitle(), board2.getTitle());
		assertEquals(board1.getContent(), board2.getContent());
		assertEquals(board1.getCreatedTime(), board2.getCreatedTime());
		assertEquals(board1.getImage(), board2.getImage());
		assertEquals(board1.getViews(), board2.getViews());		
	}
}
