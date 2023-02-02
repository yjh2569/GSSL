package com.drdoc.BackEnd.api.service;

import org.springframework.data.domain.Page;

import com.drdoc.BackEnd.api.domain.Board;
import com.drdoc.BackEnd.api.domain.dto.BoardDetailDto;
import com.drdoc.BackEnd.api.domain.dto.BoardListDto;
import com.drdoc.BackEnd.api.domain.dto.BoardModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.BoardWriteRequestDto;

public interface BoardService {
	Board writeBoard(String userId, BoardWriteRequestDto boardWriteRequestDto);
	Board modifyBoard(String userId, int boardId, BoardModifyRequestDto boardModifyRequestDto);
	String getBoardImage(int boardId);
	void deleteBoard(String userId, int boardId);
	Page<BoardListDto> getBoardList(int typeId, String word, int page, int size);
	BoardDetailDto getBoardDetail(int boardId);
}
