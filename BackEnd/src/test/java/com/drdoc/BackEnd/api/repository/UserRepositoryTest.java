package com.drdoc.BackEnd.api.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.UserModifyRequestDto;

@DataJpaTest
public class UserRepositoryTest {
	
	@Autowired
	private UserRepository userRepository;
	
	private User defaultUser;
	private final String DEFAULT_MEMBER_ID = "test";
	private final String DEFAULT_NICKNAME = "testNick";
	
	@BeforeEach
	public void setUp() {
		defaultUser = User.builder()
				.memberId(DEFAULT_MEMBER_ID)
				.email("test@test.com")
				.password("password")
				.nickname(DEFAULT_NICKNAME)
				.gender("M")
				.phone("01012345678")
				.build();
		userRepository.save(defaultUser);
	}
	
	@Test
	@DisplayName("일반 사용자 정보 저장")
	public void save() {
		User newUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname("testNick2")
				.gender("M")
				.phone("01012345678")
				.introduce("test")
				.petId(1)
				.profilePic("testPic")
				.build();
		User savedUser = userRepository.save(newUser);
		compareTwoUsers(newUser, savedUser);
	}

	@Test
	@DisplayName("중복 아이디 사용자 정보 저장")
	public void saveWithDuplicatedMemberId() {
		User newUser = User.builder()
				.memberId(DEFAULT_MEMBER_ID)
				.email("test2@test.com")
				.password("password2")
				.nickname("testNick2")
				.gender("M")
				.phone("01012345678")
				.build();
		assertThrows(DataIntegrityViolationException.class, () -> {
			userRepository.save(newUser);
		});
	}
	
	@Test
	@DisplayName("중복 닉네임 사용자 정보 저장")
	public void saveWithDuplicatedNickname() {
		User newUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname(DEFAULT_NICKNAME)
				.gender("M")
				.phone("01012345678")
				.build();
		assertThrows(DataIntegrityViolationException.class, () -> {
			userRepository.save(newUser);
		});
	}
	
	@Test
	@DisplayName("아이디를 통한 일반 사용자 정보 조회")
	public void findByMemberId() {
		Optional<User> opUser = userRepository.findByMemberId(DEFAULT_MEMBER_ID);
		assertTrue(opUser.isPresent());
		User userFromRepo = opUser.get();
		assertEquals(userFromRepo.getId(), defaultUser.getId());
		assertEquals(userFromRepo.getMemberId(), DEFAULT_MEMBER_ID);
		assertEquals(userFromRepo.getNickname(), DEFAULT_NICKNAME);
	}
	
	@Test
	@DisplayName("잘못된 아이디를 통한 사용자 정보 조회")
	public void findByWrongMemberId() {
		Optional<User> opUser = userRepository.findByMemberId("wrongMemberId");
		assertFalse(opUser.isPresent());
	}
	
	@Test
	@DisplayName("닉네임을 통한 일반 사용자 정보 조회")
	public void findByNickname() {
		Optional<User> opUser = userRepository.findByNickname(DEFAULT_NICKNAME);
		assertTrue(opUser.isPresent());
		User userFromRepo = opUser.get();
		assertEquals(userFromRepo.getId(), defaultUser.getId());
		assertEquals(userFromRepo.getMemberId(), DEFAULT_MEMBER_ID);
		assertEquals(userFromRepo.getNickname(), DEFAULT_NICKNAME);
	}
	
	@Test
	@DisplayName("잘못된 닉네임을 통한 사용자 정보 조회")
	public void findByWrongNickname() {
		Optional<User> opUser = userRepository.findByMemberId("wrongNickname");
		assertFalse(opUser.isPresent());
	}
	
	@Test
	@DisplayName("일반 사용자 정보 수정")
	public void update() {
		UserModifyRequestDto requestDto = UserModifyRequestDto.builder()
				.member_id("testM")
				.email("testM@test.com")
				.password("passwordM")
				.nickname("testNickM")
				.gender("F")
				.phone("01011112222")
				.introduce("testM")
				.pet_id(5)
				.profile_pic("testPicM")
				.build();
		defaultUser.modify(requestDto);
		User updatedUser = userRepository.save(defaultUser);
		compareTwoUsers(updatedUser, defaultUser);
	}
	
	@Test
	@DisplayName("아이디 중복으로 사용자 정보 수정 실패")
	public void updateWithDupMemberId() {
		final String DUPLICATED_MEMBER_ID = "testDup";
		User anotherUser = User.builder()
				.memberId(DUPLICATED_MEMBER_ID)
				.email("test2@test.com")
				.password("password2")
				.nickname("test2")
				.gender("M")
				.phone("01012345678")
				.build();
		userRepository.saveAndFlush(anotherUser);
		UserModifyRequestDto requestDto = UserModifyRequestDto.builder()
				.member_id(DUPLICATED_MEMBER_ID)
				.email("testM@test.com")
				.password("passwordM")
				.nickname("testNickM")
				.gender("F")
				.phone("01011112222")
				.introduce("testM")
				.pet_id(5)
				.profile_pic("testPicM")
				.build();
		defaultUser.modify(requestDto);
		assertThrows(DataIntegrityViolationException.class, () -> {			
			userRepository.saveAndFlush(defaultUser);
		});
	}
	
	@Test
	@DisplayName("닉네임 중복으로 사용자 정보 수정 실패")
	public void updateWithDupNickname() {
		final String DUPLICATED_NICKNAME = "testDup";
		User anotherUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname(DUPLICATED_NICKNAME)
				.gender("M")
				.phone("01012345678")
				.build();
		userRepository.saveAndFlush(anotherUser);
		UserModifyRequestDto requestDto = UserModifyRequestDto.builder()
				.member_id("testM")
				.email("testM@test.com")
				.password("passwordM")
				.nickname(DUPLICATED_NICKNAME)
				.gender("F")
				.phone("01011112222")
				.introduce("testM")
				.pet_id(5)
				.profile_pic("testPicM")
				.build();
		defaultUser.modify(requestDto);
		assertThrows(DataIntegrityViolationException.class, () -> {			
			userRepository.saveAndFlush(defaultUser);
		});
	}
	
	private void compareTwoUsers(User user1, User user2) {
		assertEquals(user1.getMemberId(), user2.getMemberId());
		assertEquals(user1.getEmail(), user2.getEmail());
		assertEquals(user1.getPassword(), user2.getPassword());
		assertEquals(user1.getNickname(), user2.getNickname());
		assertEquals(user1.getGender(), user2.getGender());
		assertEquals(user1.getPhone(), user2.getPhone());
		assertEquals(user1.getIntroduce(), user2.getIntroduce());
		assertEquals(user1.getPetId(), user2.getPetId());
		assertEquals(user1.getProfilePic(), user2.getProfilePic());
	}
}
