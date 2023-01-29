package com.drdoc.BackEnd.api.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.drdoc.BackEnd.api.domain.Board;
import com.drdoc.BackEnd.api.domain.BoardType;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.BoardModifyRequestDto;

@DataJpaTest
public class BoardRepositoryTest {
	
	@Autowired
	private BoardRepository boardRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BoardTypeRepository boardTypeRepository;
	
	private User defaultUser;
	private BoardType boardType1;
	private BoardType boardType2;
	private Board defaultBoard;
	
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
		boardType1 = BoardType.builder()
				.title("T1")
				.build();
		boardTypeRepository.save(boardType1);
		boardType2 = BoardType.builder()
				.title("T2")
				.build();
		boardTypeRepository.save(boardType2);
		defaultBoard = Board.builder()
				.user(defaultUser)
				.type(boardType1)
				.title("test")
				.content("testContent")
				.image("testImg")
				.createdTime(LocalDateTime.now())
				.views(10)
				.build();
		boardRepository.save(defaultBoard);
	}
	
	@Test
	@DisplayName("board 저장")
	public void save() {
		Board newBoard = Board.builder()
				.user(defaultUser)
				.type(boardType1)
				.title("test2")
				.content("testContent2")
				.image("testImg2")
				.createdTime(LocalDateTime.now())
				.views(15)
				.build();
		Board savedBoard = boardRepository.save(newBoard);
		compareTwoBoards(newBoard, savedBoard);
	}
	
	@Test
	@DisplayName("type과 title을 통한 board 리스트 조회")
	public void findByTypeIdAndTitleContains() {
		Board boardWithType1AndTitleSearch = Board.builder()
				.user(defaultUser)
				.type(boardType1)
				.title("testsearch")
				.content("testContent")
				.image("testImg")
				.createdTime(LocalDateTime.now())
				.views(10)
				.build();
		boardRepository.saveAndFlush(boardWithType1AndTitleSearch);
		Board boardWithType2AndTitleNotSearch = Board.builder()
				.user(defaultUser)
				.type(boardType2)
				.title("test")
				.content("testContent")
				.image("testImg")
				.createdTime(LocalDateTime.now())
				.views(10)
				.build();
		boardRepository.saveAndFlush(boardWithType2AndTitleNotSearch);
		Board boardWithType2AndTitleSearch = Board.builder()
				.user(defaultUser)
				.type(boardType2)
				.title("testsearch")
				.content("testContent")
				.image("testImg")
				.createdTime(LocalDateTime.now())
				.views(10)
				.build();
		boardRepository.saveAndFlush(boardWithType2AndTitleSearch);
		int page = 0, size = 4;
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
		List<Board> boardsFromRepo = boardRepository.
				findByTypeIdAndTitleContains(boardType1.getId(), "search", pageable)
				.stream().collect(Collectors.toList());
		assertEquals(boardsFromRepo.size(), 1);
		compareTwoBoards(boardsFromRepo.get(0), boardWithType1AndTitleSearch);
	}
	
	@Test
	@DisplayName("board 상세 조회")
	public void findById() {
		Optional<Board> opBoard = boardRepository.findById(defaultBoard.getId());
		assertTrue(opBoard.isPresent());
		Board boardFromRepo = opBoard.get();
		compareTwoBoards(boardFromRepo, defaultBoard);
	}
	
	@Test
	@DisplayName("board 수정")
	public void update() {
		BoardModifyRequestDto requestDto = BoardModifyRequestDto.builder()
				.type_id(boardType2.getId())
				.title("testM")
				.content("testContentM")
				.image("testImgM")
				.build();
		defaultBoard.modify(requestDto, boardType2);
		Board updatedBoard = boardRepository.save(defaultBoard);
		compareTwoBoards(updatedBoard, defaultBoard);
	}
	
	@Test
	@DisplayName("board 삭제")
	public void delete() {
		boardRepository.delete(defaultBoard);
		Optional<Board> opBoard = boardRepository.findById(defaultBoard.getId());
		assertFalse(opBoard.isPresent());
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
