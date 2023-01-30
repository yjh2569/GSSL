package com.drdoc.BackEnd.api.service;

import java.util.List;

import com.drdoc.BackEnd.api.domain.dto.CommentListDto;
import com.drdoc.BackEnd.api.domain.dto.CommentModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.CommentWriteRequestDto;

public interface CommentService {
	void writeComment(CommentWriteRequestDto requestDto);
	void modifyComment(int commentId, CommentModifyRequestDto requestDto);
	void deleteComment(int commentId);
	List<CommentListDto> getCommentList(int boardId);
}
