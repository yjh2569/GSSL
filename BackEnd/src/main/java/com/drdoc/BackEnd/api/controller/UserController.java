package com.drdoc.BackEnd.api.controller;

import javax.validation.Valid;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.drdoc.BackEnd.api.domain.dto.BaseResponseDto;
import com.drdoc.BackEnd.api.domain.dto.RefreshTokenDto;
import com.drdoc.BackEnd.api.domain.dto.TokenDto;
import com.drdoc.BackEnd.api.domain.dto.UserInfoResponseDto;
import com.drdoc.BackEnd.api.domain.dto.UserLoginRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserLoginResponseDto;
import com.drdoc.BackEnd.api.domain.dto.UserLogoutRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserPetModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserRegisterRequestDto;
import com.drdoc.BackEnd.api.service.FileUploadService;
import com.drdoc.BackEnd.api.service.UserService;
import com.drdoc.BackEnd.api.util.SecurityUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "유저 API", tags = { "User 관리" })
@RestController
@CrossOrigin("*")
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private FileUploadService fileUploadService;

	@Autowired
	private UserService userService;

	@ApiOperation(value = "회원가입", notes = "유저 정보 삽입")
	@PostMapping("/public/signup")
	@ApiResponses({ @ApiResponse(code = 200, message = "성공"), @ApiResponse(code = 400, message = "부적절한 요청"),
			@ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> register(
			@RequestPart(value = "user") @Valid UserRegisterRequestDto requestDto,
			@RequestPart(value = "file", required = false) MultipartFile file) throws FileUploadException {
		String imgPath = fileUploadService.uploadFile(file);
		requestDto.setProfile_pic(imgPath);
		userService.register(requestDto);
		return ResponseEntity.status(201).body(BaseResponseDto.of(201, "Created"));
	}

	@ApiOperation(value = "아이디 중복체크", notes = "memberId 중복체크")
	@GetMapping("/public/id/{id}")
	@ApiResponses({ @ApiResponse(code = 200, message = "사용가능한 아이디입니다."),
			@ApiResponse(code = 400, message = "중복된 아이디입니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> checkMemberId(@PathVariable("id") String memberId) {
		if (userService.checkMemberId(memberId)) {
			return ResponseEntity.status(200).body(BaseResponseDto.of(200, "사용가능한 아이디입니다."));
		}
		return ResponseEntity.status(400).body(BaseResponseDto.of(400, "중복된 아이디입니다."));
	}

	@ApiOperation(value = "닉네임 중복체크", notes = "nickname 중복체크")
	@GetMapping("/public/nickname/{nickname}")
	@ApiResponses({ @ApiResponse(code = 200, message = "사용가능한 닉네임입니다."),
			@ApiResponse(code = 400, message = "중복된 닉네임입니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> checkNickname(@PathVariable("nickname") String nickname) {
		if (userService.checkNickname(nickname)) {
			return ResponseEntity.status(200).body(BaseResponseDto.of(200, "사용가능한 닉네임입니다."));
		}
		return ResponseEntity.status(400).body(BaseResponseDto.of(400, "중복된 닉네임입니다."));
	}

	@ApiOperation(value = "로그인", notes = "memberId와 password를 사용해 로그인")
	@PostMapping("/public/login")
	@ApiResponses({ @ApiResponse(code = 200, message = "로그인 성공"),
			@ApiResponse(code = 400, message = "회원정보가 일치하지 않습니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
		TokenDto tokenDto = userService.login(userLoginRequestDto);
		return ResponseEntity.status(200).body(UserLoginResponseDto.of(200, "로그인 성공", tokenDto));
	}

	@ApiOperation(value = "Access 토큰 재발급", notes = "JWT Refresh 토큰을 사용해 재발급")
	@PostMapping("/auth/reissue")
	@ApiResponses({ @ApiResponse(code = 200, message = "재발급 성공"), @ApiResponse(code = 400, message = "정보가 일치하지 않습니다."),
			@ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> reissue(@RequestBody RefreshTokenDto tokenRequestDto) {
		TokenDto tokenDto = userService.reissue(tokenRequestDto);
		return ResponseEntity.status(200).body(UserLoginResponseDto.of(200, "재발급 성공", tokenDto));
	}

	@ApiOperation(value = "로그아웃", notes = "현재 사용중인 Refresh Token을 DB에서 삭제")
	@PostMapping("/auth/logout")
	@ApiResponses({ @ApiResponse(code = 200, message = "로그아웃 성공"),
			@ApiResponse(code = 400, message = "회원정보가 일치하지 않습니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> logout(@RequestBody UserLogoutRequestDto userLogoutRequestDto) {
		userService.logout(userLogoutRequestDto.getRefresh_token());
		return ResponseEntity.status(200).body(BaseResponseDto.of(200, "로그아웃 성공"));
	}
	
	@ApiOperation(value = "회원정보 조회", notes = "회원 아이디를 이용해 회원정보를 조회합니다.")
	@GetMapping
	@ApiResponses({ @ApiResponse(code = 200, message = "회원정보를 성공적으로 불러왔습니다."),
			@ApiResponse(code = 400, message = "가입하지 않거나 탈퇴한 회원입니다."), 
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> getUserDetail() {
		String memberId = SecurityUtil.getCurrentUsername();
		return ResponseEntity.status(200).body(UserInfoResponseDto.of(200, "Success", userService.getUserDetail(memberId)));
	}
	
	@ApiOperation(value = "회원정보 수정", notes = "입력한 정보을 바탕으로 회원정보를 수정합니다.")
	@PutMapping
	@ApiResponses({ @ApiResponse(code = 200, message = "회원정보를 성공적으로 수정했습니다."),
			@ApiResponse(code = 400, message = "가입하지 않거나 탈퇴한 회원입니다. 또는 입력이 잘못되었습니다."), 
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> modify(@RequestPart(value = "user") @Valid UserModifyRequestDto requestDto,
			@RequestPart(value = "file", required = false) MultipartFile file) throws FileUploadException {
		String memberId = SecurityUtil.getCurrentUsername();
		String currentFilePath = userService.getProfilePicture(memberId);
		if (file != null) {
			String imgPath = fileUploadService.modifyFile(currentFilePath, file);
			requestDto.setProfile_pic(imgPath);
		} else {
			fileUploadService.deleteFile(currentFilePath);
		}
		userService.modify(memberId, requestDto);
		return ResponseEntity.status(200).body(BaseResponseDto.of(200, "Modified"));
	}
	
	@ApiOperation(value = "회원정보 수정", notes = "입력한 정보을 바탕으로 회원정보를 수정합니다.")
	@PutMapping("/pet")
	@ApiResponses({ @ApiResponse(code = 200, message = "회원정보를 성공적으로 수정했습니다."),
			@ApiResponse(code = 400, message = "가입하지 않거나 탈퇴한 회원입니다. 또는 입력이 잘못되었습니다."), 
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> modifyPet(@RequestBody UserPetModifyRequestDto requestDto) {
		String memberId = SecurityUtil.getCurrentUsername();
		userService.modifyPet(memberId, requestDto.getPet_id());
		return ResponseEntity.status(200).body(BaseResponseDto.of(200, "Modified"));
	}
	
	@ApiOperation(value = "회원탈퇴", notes = "회원탈퇴를 수행합니다. 이후 현재 계정으로 로그인 불가능합니다.")
	@PutMapping("/leave")
	@ApiResponses({ @ApiResponse(code = 200, message = "회원탈퇴에 성공했습니다."),
			@ApiResponse(code = 400, message = "가입하지 않거나 이미 탈퇴한 회원입니다."), 
			@ApiResponse(code = 401, message = "인증이 만료되어 로그인이 필요합니다."), @ApiResponse(code = 500, message = "서버 오류") })
	public ResponseEntity<BaseResponseDto> quit() {
		String memberId = SecurityUtil.getCurrentUsername();
		userService.quit(memberId);
		return ResponseEntity.status(200).body(BaseResponseDto.of(200, "Modified"));
	}
}
