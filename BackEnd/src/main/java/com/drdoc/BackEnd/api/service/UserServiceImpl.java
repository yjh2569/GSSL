package com.drdoc.BackEnd.api.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drdoc.BackEnd.api.domain.Pet;
import com.drdoc.BackEnd.api.domain.RefreshToken;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.RefreshTokenDto;
import com.drdoc.BackEnd.api.domain.dto.TokenDto;
import com.drdoc.BackEnd.api.domain.dto.UserInfoDto;
import com.drdoc.BackEnd.api.domain.dto.UserLoginRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserRegisterRequestDto;
import com.drdoc.BackEnd.api.jwt.TokenProvider;
import com.drdoc.BackEnd.api.repository.PetRepository;
import com.drdoc.BackEnd.api.repository.RefreshTokenRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository repository;
	private final PasswordEncoder encoder;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final TokenProvider tokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PetRepository petRepository;

	// 회원가입
	@Override
	public User register(UserRegisterRequestDto requestDto) {
		checkDuplication(requestDto);
		User user = User.builder().memberId(requestDto.getMember_id()).nickname(requestDto.getNickname())
				.password(encoder.encode(requestDto.getPassword().toLowerCase())).email(requestDto.getEmail())
				.phone(requestDto.getPhone()).gender(requestDto.getGender()).profilePic(requestDto.getProfile_pic())
				.introduce(requestDto.getIntroduce()).build();
		return repository.save(user);
	}

	// memberId 중복체크
	public boolean checkMemberId(String memberId) {
		Optional<User> user = repository.findByMemberId(memberId);
		return !user.isPresent();
	};

	// 닉네임 중복체크
	public boolean checkNickname(String nickname) {
		Optional<User> user = repository.findByNickname(nickname);
		return !user.isPresent();
	};

	@Transactional
	public TokenDto login(UserLoginRequestDto userLoginRequestDto) {
		User user = repository.findByMemberId(userLoginRequestDto.getMember_id())
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		if (user.isLeft())
			throw new IllegalArgumentException("이미 탈퇴한 회원입니다.");
		// 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				userLoginRequestDto.getMember_id(), userLoginRequestDto.getPassword().toLowerCase());
		// 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
		// authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername
		// 메서드가 실행됨
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// 3. 인증 정보를 기반으로 JWT 토큰 생성
		TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

		// 4. RefreshToken 저장
		RefreshToken refreshToken = RefreshToken.builder().key(authentication.getName())
				.value(tokenDto.getRefreshToken()).build();
		refreshTokenRepository.save(refreshToken);

		// 5. 토큰 발급
		return tokenDto;
	}

	@Transactional
	public TokenDto reissue(RefreshTokenDto tokenRequestDto) {
		// 1. Refresh Token 검증
		if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
			throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
		}

		// 2. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
		RefreshToken refreshToken = refreshTokenRepository.findByValue(tokenRequestDto.getRefreshToken())
				.orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

		// 3. Refresh Token 에서 Member ID 가져오기
		String memberId = refreshToken.getKey();

		// 4. 새로운 토큰 생성
		TokenDto tokenDto = tokenProvider.generateTokenDto(memberId);

		// 6. 저장소 정보 업데이트
		RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
		refreshTokenRepository.save(newRefreshToken);

		// 토큰 발급
		return tokenDto;
	}

	@Transactional
	public void logout(String refresh_token) {
		refreshTokenRepository.deleteByValue(refresh_token);
	}

	@Transactional
	@Scheduled(cron = "0 * */12 * * ?") // 12시간에 1번 시간만료된 RefreshToken DB에서 삭제
	public void clean() {
		refreshTokenRepository.deleteByExpireTimeLessThan(LocalDateTime.now());
	}

	@Override
	public UserInfoDto getUserDetail(String memberId) {
		User user = repository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		if (user.isLeft())
			throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
		UserInfoDto userInfoDto = UserInfoDto.builder().member_id(memberId).email(user.getEmail())
				.gender(user.getGender()).introduce(user.getIntroduce()).leave(user.isLeft())
				.nickname(user.getNickname()).phone(user.getPhone()).profile_pic(user.getProfilePic())
				.pet_id(user.getPetId()).build();
		return userInfoDto;
	}
	
	@Override
	public User modify(String memberId, UserModifyRequestDto requestDto) {
		checkModifyDuplication(memberId, requestDto);
		User user = repository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		if (user.isLeft())
			throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
		requestDto.setPassword(encoder.encode(requestDto.getPassword()));
		user.modify(requestDto);
		return repository.save(user);
	}
	
	@Override
	public User modifyPet(String memberId, int petId) {
		User user = repository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		if (user.isLeft())
			throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
		if (petId != 0) {
			Pet pet = petRepository.findById(petId)
					.orElseThrow(() -> new IllegalArgumentException("등록되지 않은 반려동물입니다."));
			if (pet.getUser().getId() != user.getId()) {
				throw new IllegalArgumentException("해당 반려동물은 선택할 수 없습니다.");
			}
		}
		user.modifyPetId(petId);
		return repository.save(user);
	}

	@Override
	public String getProfilePicture(String memberId) {
		User user = repository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		if (user.isLeft())
			throw new IllegalArgumentException("이미 탈퇴한 계정입니다.");
		return user.getProfilePic();
	}

	public void checkDuplication(UserRegisterRequestDto requestDto) {
		if (!checkMemberId(requestDto.getMember_id())) {
			throw new IllegalArgumentException("중복된 아이디입니다.");
		}
		if (!checkNickname(requestDto.getNickname())) {
			throw new IllegalArgumentException("중복된 닉네임입니다.");
		}
	}

	public void checkModifyDuplication(String memberId, UserModifyRequestDto requestDto) {
		User user = repository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		if (!memberId.equals(requestDto.getMember_id()) && !checkMemberId(requestDto.getMember_id())) {
			throw new IllegalArgumentException("중복된 아이디입니다.");
		}
		if (!user.getNickname().equals(requestDto.getNickname()) && !checkNickname(requestDto.getNickname())) {
			throw new IllegalArgumentException("중복된 닉네임입니다.");
		}
	}

	@Override
	public User quit(String memberId) {
		User user = repository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		user.quit();
		return repository.save(user);
	}
}
