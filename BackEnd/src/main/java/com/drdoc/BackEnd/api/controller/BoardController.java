package com.drdoc.BackEnd.api.controller;

import javax.validation.Valid;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.drdoc.BackEnd.api.domain.dto.BaseResponseDto;
import com.drdoc.BackEnd.api.domain.dto.BoardDetailDto;
import com.drdoc.BackEnd.api.domain.dto.BoardDetailResponseDto;
import com.drdoc.BackEnd.api.domain.dto.BoardListDto;
import com.drdoc.BackEnd.api.domain.dto.BoardListResponseDto;
import com.drdoc.BackEnd.api.domain.dto.BoardModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.BoardWriteRequestDto;
import com.drdoc.BackEnd.api.service.BoardService;
import com.drdoc.BackEnd.api.service.FileUploadService;
import com.drdoc.BackEnd.api.util.SecurityUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "커뮤니티 API", tags = { "Board 관리" })
@RestController
@CrossOrigin("*")
@RequestMapping("/api/board")
public class BoardController {

	@Autowired
	private FileUploadService fileUploadService;

	@Autowired
	private BoardService boardService;

	@PostMapping
	@ApiOperation(value = "커뮤니티 게시글 등록", notes = "커뮤니티에 게시글을 등록합니다.")
	@ApiResponses({ @ApiResponse(code = 201, message = "게시글 등록에 성공했습니다."),
			@ApiResponse(code = 400, message = "입력이 잘못되었거나 입력 제한을 넘어갔습니다."),
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> writeBoard(
			@Valid @RequestPart(value = "board") BoardWriteRequestDto requestDto,
			@RequestPart(value = "file", required = false) MultipartFile file) throws FileUploadException {
		String memberId = SecurityUtil.getCurrentUsername();
		String ImgPath = fileUploadService.uploadFile(file);
		requestDto.setImage(ImgPath);
		boardService.writeBoard(memberId, requestDto);
		return ResponseEntity.status(201).body(BaseResponseDto.of(201, "Created"));
	}

	@PutMapping("/{boardId}")
	@ApiOperation(value = "커뮤니티 게시글 수정", notes = "커뮤니티 내 게시글을 수정합니다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "게시글 수정에 성공했습니다."),
			@ApiResponse(code = 400, message = "입력이 잘못되었거나 입력 제한을 넘어갔습니다."),
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."),
			@ApiResponse(code = 403, message = "게시글 수정 권한이 없습니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> modifyBoard(@PathVariable("boardId") int boardId,
			@Valid @RequestPart(value = "board") BoardModifyRequestDto requestDto,
			@RequestPart(value = "file", required = false) MultipartFile file) throws FileUploadException {
		String memberId = SecurityUtil.getCurrentUsername();
		String currentFilePath = boardService.getBoardImage(boardId);
		if (file != null) {
			String imgPath = fileUploadService.modifyFile(currentFilePath, file);
			requestDto.setImage(imgPath);
		} else {
			fileUploadService.deleteFile(currentFilePath);
		}
		boardService.modifyBoard(memberId, boardId, requestDto);
		return ResponseEntity.status(200).body(BaseResponseDto.of(200, "Modified"));
	}

	@DeleteMapping("/{boardId}")
	@ApiOperation(value = "커뮤니티 게시글 삭제", notes = "커뮤니티 내 게시글을 삭제합니다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "게시글 삭제에 성공했습니다."),
			@ApiResponse(code = 400, message = "입력이 잘못되었습니다."),
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."),
			@ApiResponse(code = 403, message = "게시글 삭제 권한이 없습니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> deleteBoard(@PathVariable("boardId") int boardId) throws FileUploadException {
		String memberId = SecurityUtil.getCurrentUsername();
		String image = boardService.getBoardImage(boardId);
		if (image != null && !"".equals(image)) {
			fileUploadService.deleteFile(image);
		}
		boardService.deleteBoard(memberId, boardId);
		return ResponseEntity.status(200).body(BaseResponseDto.of(200, "Deleted"));
	}

	@GetMapping
	@ApiOperation(value = "커뮤니티 게시글 전체 조회", notes = "특정 유형 번호에 대한 커뮤니티 내 전체 게시글을 조회합니다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "게시글 전체 조회에 성공했습니다."),
			@ApiResponse(code = 400, message = "입력이 잘못되었습니다."),
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<? extends BaseResponseDto> boardList(@RequestParam("type_id") int typeId, @RequestParam("word") String word,
			@RequestParam("page") int page, @RequestParam("size") int size) {
		Page<BoardListDto> boardList = boardService.getBoardList(typeId, word, page, size);
		return ResponseEntity.status(200).body(BoardListResponseDto.of(200, "Success", boardList));
	}

	@GetMapping("/{boardId}")
	@ApiOperation(value = "커뮤니티 게시글 상세 조회", notes = "특정 게시글을 상세 조회합니다.")
	@ApiResponses({ @ApiResponse(code = 200, message = "게시글 상세 조회에 성공했습니다."),
			@ApiResponse(code = 400, message = "입력이 잘못되었습니다."),
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<? extends BaseResponseDto> boardDetail(@PathVariable("boardId") int boardId) {
		BoardDetailDto board = boardService.getBoardDetail(boardId);
		return ResponseEntity.status(200).body(BoardDetailResponseDto.of(200, "Success", board));
	}
}
