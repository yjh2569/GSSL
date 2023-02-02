package com.drdoc.BackEnd.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.drdoc.BackEnd.api.domain.Board;
import com.drdoc.BackEnd.api.domain.BoardType;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.BoardDetailDto;
import com.drdoc.BackEnd.api.domain.dto.BoardListDto;
import com.drdoc.BackEnd.api.domain.dto.BoardModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.BoardWriteRequestDto;
import com.drdoc.BackEnd.api.repository.BoardRepository;
import com.drdoc.BackEnd.api.repository.BoardTypeRepository;
import com.drdoc.BackEnd.api.repository.CommentRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

@Service
public class BoardServiceImpl implements BoardService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BoardTypeRepository boardTypeRepository;

	@Autowired
	private BoardRepository boardRepository;
	
	@Autowired
	private CommentRepository commentRepository;

	@Override
	@Transactional
	public Board writeBoard(String userId, BoardWriteRequestDto boardWriteRequestDto) {
		User user = userRepository.findByMemberId(userId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		BoardType boardType = boardTypeRepository.findById(boardWriteRequestDto.getType_id())
				.orElseThrow(() -> new IllegalArgumentException("게시글 유형 번호가 올바르지 않습니다."));
		Board board = Board.builder().user(user).type(boardType).title(boardWriteRequestDto.getTitle())
				.content(boardWriteRequestDto.getContent()).image(boardWriteRequestDto.getImage())
				.createdTime(LocalDateTime.now()).comments(new ArrayList<>()).build();
		return boardRepository.save(board);
	}

	@Override
	@Transactional
	public Board modifyBoard(String userId, int boardId, BoardModifyRequestDto boardModifyRequestDto) {
		User user = userRepository.findByMemberId(userId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		Board board = boardRepository.findById(boardId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		BoardType boardType = boardTypeRepository.findById(boardModifyRequestDto.getType_id())
				.orElseThrow(() -> new IllegalArgumentException("게시글 유형 번호가 올바르지 않습니다."));
		if (user.getId() != board.getUser().getId())
			throw new AccessDeniedException("권한이 없습니다.");
		board.modify(boardModifyRequestDto, boardType);
		return boardRepository.save(board);
	}

	@Override
	@Transactional
	public String getBoardImage(int boardId) {
		Board board = boardRepository.findById(boardId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		return board.getImage();
	}

	@Override
	@Transactional
	public void deleteBoard(String userId, int boardId) {
		User user = userRepository.findByMemberId(userId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		Board board = boardRepository.findById(boardId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		if (user.getId() != board.getUser().getId())
			throw new AccessDeniedException("권한이 없습니다.");
		board.getComments().stream()
			.forEach((comment) -> commentRepository.delete(comment));
		boardRepository.delete(board);
	}

	@Override
	@Transactional
	public Page<BoardListDto> getBoardList(int typeId, String word, int page, int size) {
		List<BoardListDto> boards = boardRepository
				.findByTypeIdAndTitleContains(typeId, word, PageRequest.of(page, size, Sort.by("id").descending())).stream()
				.map(BoardListDto::new).collect(Collectors.toList());
		return new PageImpl<>(boards);
	}

	@Override
	@Transactional
	public BoardDetailDto getBoardDetail(int boardId) {
		Board board = boardRepository.findById(boardId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		return new BoardDetailDto(board);
	}
}
