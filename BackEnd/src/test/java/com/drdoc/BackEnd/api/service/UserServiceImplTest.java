package com.drdoc.BackEnd.api.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drdoc.BackEnd.api.domain.Kind;
import com.drdoc.BackEnd.api.domain.Pet;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.UserInfoDto;
import com.drdoc.BackEnd.api.domain.dto.UserModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.UserRegisterRequestDto;
import com.drdoc.BackEnd.api.repository.PetRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private PetRepository petRepository;
	
	@InjectMocks
	private UserServiceImpl userService;
	
	private User defaultUser;
	private User anotherUser;
	private Pet defaultPet;
	
	private final String MEMBER_ID = "test";
	private final String MODIFIED_MEMBER_ID = "modified";
	private final String MODIFIED_NICKNAME = "modified";
	private final String PROFILE_PICTURE = "testPic";
	
	@BeforeEach
	public void setUp() {
		defaultUser = User.builder()
				.id(1)
				.memberId(MEMBER_ID)
				.email("test@test.com")
				.password("password")
				.nickname("testNick")
				.gender("M")
				.phone("01012345678")
				.profilePic(PROFILE_PICTURE)
				.build();
		anotherUser = User.builder()
				.id(2)
				.memberId(MODIFIED_MEMBER_ID)
				.email("test@test.com")
				.password("password")
				.nickname(MODIFIED_NICKNAME)
				.gender("M")
				.phone("01012345678")
				.build();
		Kind defaultKind = Kind.builder()
				.name("kindName")
				.build();
		defaultPet = Pet.builder()
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("test")
				.gender("M")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(10.0f)
				.animalPic("testPic")
				.death(false)
				.diseases("testDisease")
				.description("testDiscription")
				.build();
	}
	
	@Test
	@DisplayName("회원가입")
	public void register() {
		when(userRepository.save(any(User.class))).thenReturn(defaultUser);
		
		UserRegisterRequestDto requestDto = prepareRegister();
		User savedUser = userService.register(requestDto);
		compareTwoUsers(savedUser, defaultUser);
	
	}
	
	@Test
	@DisplayName("아이디 중복 시 회원가입 실패")
	public void registerWithSameId() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		
		UserRegisterRequestDto requestDto = prepareRegister();
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.register(requestDto);
		});		
	}
	
	@Test
	@DisplayName("닉네임 중복 시 회원가입 실패")
	public void registerWithSameNickname() {
		when(userRepository.findByNickname("testNick")).thenReturn(Optional.of(defaultUser));
		
		UserRegisterRequestDto requestDto = prepareRegister();
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.register(requestDto);
		});		
	}
	
	@Test
	@DisplayName("아이디 중복 체크")
	public void checkMemberId() {
		String unique = "unique";
		String duplicated = MEMBER_ID;
		
		when(userRepository.findByMemberId(unique)).thenReturn(Optional.empty());
		when(userRepository.findByMemberId(duplicated)).thenReturn(Optional.of(defaultUser));
		
		assertTrue(userService.checkMemberId(unique));
		assertFalse(userService.checkMemberId(duplicated));
	}
	
	@Test
	@DisplayName("닉네임 중복 체크")
	public void checkNickname() {
		String unique = "unique";
		String duplicated = "testNick";
		
		when(userRepository.findByNickname(unique)).thenReturn(Optional.empty());
		when(userRepository.findByNickname(duplicated)).thenReturn(Optional.of(defaultUser));
		
		assertTrue(userService.checkNickname(unique));
		assertFalse(userService.checkNickname(duplicated));
	}
	
	@Test
	@DisplayName("사용자 상세 정보 확인")
	public void getUserDetail() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		UserInfoDto userInfoDto = userService.getUserDetail(MEMBER_ID);
		assertEquals(userInfoDto.getMember_id(), MEMBER_ID);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 상세 정보 확인 실패")
	public void getUserDetailForNotExistedUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.getUserDetail(MEMBER_ID);
		});
	}
	
	@Test
	@DisplayName("탈퇴한 사용자 상세 정보 확인 실패")
	public void getUserDetailForQuittedUser() {
		defaultUser.quit();
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.getUserDetail(MEMBER_ID);
		});
	}
	
	@Test
	@DisplayName("사용자 정보 수정")
	public void modify() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.findByMemberId(MODIFIED_MEMBER_ID)).thenReturn(Optional.empty());
		when(userRepository.findByNickname(MODIFIED_NICKNAME)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(defaultUser);
		UserModifyRequestDto requestDto = prepareModify();
		User modifiedUser = userService.modify(MEMBER_ID, requestDto);
		compareTwoUsers(modifiedUser, defaultUser);
	}
	
	@Test
	@DisplayName("가입하지 않은 아이디로 사용자 정보 수정 실패")
	public void modifyWithNotExistedMemberId() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		UserModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {
			userService.modify(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("탈퇴한 사용자 상세 정보 확인 실패")
	public void modifyForQuittedUser() {
		defaultUser.quit();
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		UserModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.modify(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("중복된 아이디로 사용자 정보 수정 실패")
	public void modifyWithDuplicatedMemberId() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.findByMemberId(MODIFIED_MEMBER_ID)).thenReturn(Optional.of(anotherUser));
		UserModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {
			userService.modify(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("중복된 닉네임으로 사용자 정보 수정 실패")
	public void modifyWithDuplicatedNickname() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.findByMemberId(MODIFIED_MEMBER_ID)).thenReturn(Optional.empty());
		when(userRepository.findByNickname(MODIFIED_NICKNAME)).thenReturn(Optional.of(anotherUser));
		UserModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {
			userService.modify(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("사용자 정보 중 아이디 미변경 수정")
	public void modifyWithMemberIdNotChanged() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.findByNickname(MODIFIED_NICKNAME)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(defaultUser);
		UserModifyRequestDto requestDto = prepareModify();
		requestDto.setMember_id(MEMBER_ID);
		User modifiedUser = userService.modify(MEMBER_ID, requestDto);
		compareTwoUsers(modifiedUser, defaultUser);
	}
	
	@Test
	@DisplayName("사용자 정보 중 닉네임 미변경 수정")
	public void modifyWithNicknameNotChanged() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.findByMemberId(MODIFIED_MEMBER_ID)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(defaultUser);
		UserModifyRequestDto requestDto = prepareModify();
		requestDto.setNickname(defaultUser.getNickname());
		User modifiedUser = userService.modify(MEMBER_ID, requestDto);
		compareTwoUsers(modifiedUser, defaultUser);
	}
	
	@Test
	@DisplayName("메인 펫 수정")
	public void modifyPet() {
		int newPetId = 1;
		when(petRepository.findById(newPetId)).thenReturn(Optional.of(defaultPet));
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.save(any(User.class))).thenReturn(defaultUser);
		User updatedUser = userService.modifyPet(MEMBER_ID, newPetId);
		assertEquals(updatedUser.getPetId(), newPetId);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 메인 펫 수정 실패")
	public void modifyPetNotRegisteredUser() {
		int newPetId = 1;
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.modifyPet(MEMBER_ID, newPetId);
		});
	}
	
	@Test
	@DisplayName("탈퇴한 사용자의 메인 펫 수정 실패")
	public void modifyPetQuittedUser() {
		int newPetId = 1;
		defaultUser.quit();
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.modifyPet(MEMBER_ID, newPetId);
		});
	}
	
	@Test
	@DisplayName("등록되지 않은 펫인 경우 메인 펫 수정 실패")
	public void modifyPetNotRegistered() {
		int newPetId = 1;
		when(petRepository.findById(newPetId)).thenReturn(Optional.empty());
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		assertThrows(IllegalArgumentException.class, () -> {
			userService.modifyPet(MEMBER_ID, newPetId);			
		});
	}
	
	@Test
	@DisplayName("사용자의 펫이 아닌 경우 메인 펫 수정 실패")
	public void modifyPetUserDoesNotHave() {
		int newPetId = 1;
		when(petRepository.findById(newPetId)).thenReturn(Optional.of(defaultPet));
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(anotherUser));
		assertThrows(IllegalArgumentException.class, () -> {
			userService.modifyPet(MEMBER_ID, newPetId);		
		});
	}
	
	@Test
	@DisplayName("사용자 프로필 사진 경로 조회")
	public void getProfilePicture() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		String profilePic = userService.getProfilePicture(MEMBER_ID);
		assertEquals(profilePic, PROFILE_PICTURE);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 프로필 사진 경로 조회 실패")
	public void getProfilePictureOfNotRegisteredUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.getProfilePicture(MEMBER_ID);
		});
	}
	
	@Test
	@DisplayName("탈퇴한 사용자 프로필 사진 경로 조회 실패")
	public void getProfilePictureOfQuittedUser() {
		defaultUser.quit();
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.getProfilePicture(MEMBER_ID);
		});
	}
	
	@Test
	@DisplayName("회원 탈퇴")
	public void quit() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(userRepository.save(any(User.class))).thenReturn(defaultUser);
		User quittedUser = userService.quit(MEMBER_ID);
		assertTrue(quittedUser.isLeft());
	}
	
	@Test
	@DisplayName("가입하지 않은 회원 탈퇴 실패")
	public void quitNotRegistedUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			userService.quit(MEMBER_ID);
		});
	}
	
	public UserRegisterRequestDto prepareRegister() {
		return UserRegisterRequestDto.builder()
				.member_id("test")
				.email("test@test.com")
				.password("password")
				.nickname("testNick")
				.gender("M")
				.phone("01012345678")
				.build();
	}
	
	public UserModifyRequestDto prepareModify() {
		return UserModifyRequestDto.builder()
				.member_id(MODIFIED_MEMBER_ID)
				.password("modifiedPw")
				.email("modified@test.com")
				.gender("F")
				.nickname(MODIFIED_NICKNAME)
				.phone("01012345677")
				.profile_pic("modified")
				.pet_id(3)
				.introduce("modifiedIntro")
				.build();
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
