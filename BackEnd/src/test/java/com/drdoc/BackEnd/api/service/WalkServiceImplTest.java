package com.drdoc.BackEnd.api.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
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

import com.drdoc.BackEnd.api.domain.Kind;
import com.drdoc.BackEnd.api.domain.Pet;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.Walk;
import com.drdoc.BackEnd.api.domain.WalkPet;
import com.drdoc.BackEnd.api.domain.dto.WalkDetailDto;
import com.drdoc.BackEnd.api.domain.dto.WalkModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.WalkRegisterRequestDto;
import com.drdoc.BackEnd.api.domain.dto.WalkTimeDto;
import com.drdoc.BackEnd.api.repository.PetRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;
import com.drdoc.BackEnd.api.repository.WalkPetRepository;
import com.drdoc.BackEnd.api.repository.WalkRepository;

@ExtendWith(MockitoExtension.class)
public class WalkServiceImplTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private WalkRepository walkRepository;
	
	@Mock
	private PetRepository petRepository;
	
	@Mock
	private WalkPetRepository walkPetRepository;
	
	@InjectMocks
	private WalkServiceImpl walkService;
	
	private User defaultUser;
	private User anotherUser;
	private Pet defaultPet;
	private Pet anotherPet;
	private Pet theOtherPet;
	private Kind defaultKind;
	private Walk defaultWalk;
	private Walk anotherWalk;
	private WalkPet defaultWalkPet;
	private WalkPet walkPetWithDPAW;
	private WalkPet walkPetWithAPDW;
	private WalkPet walkPetWithAPAW;
	
	private final String MEMBER_ID = "test";
	
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
		anotherUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname("testNick2")
				.gender("F")
				.phone("01011112222")
				.build();
		defaultKind = Kind.builder()
				.name("kindName")
				.build();
		defaultPet = Pet.builder()
				.id(1)
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
		anotherPet = Pet.builder()
				.id(2)
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("testA")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(12.0f)
				.animalPic("testPicA")
				.death(false)
				.diseases("testDA")
				.description("testDiscriptionA")
				.build();
		theOtherPet = Pet.builder()
				.id(3)
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("testB")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(14.0f)
				.animalPic("testPicB")
				.death(false)
				.diseases("testDB")
				.description("testDiscriptionB")
				.build();
		defaultWalk = Walk.builder()
				.id(1)
				.user(defaultUser)
				.distance(10)
				.start_time(LocalDateTime.now().minusMinutes(30))
				.end_time(LocalDateTime.now())
				.build();
		anotherWalk = Walk.builder()
				.id(2)
				.user(defaultUser)
				.distance(12)
				.start_time(LocalDateTime.now().minusMinutes(20))
				.end_time(LocalDateTime.now())
				.build();
		defaultWalkPet = WalkPet.builder()
				.walk(defaultWalk)
				.pet(defaultPet)
				.build();
		walkPetWithDPAW = WalkPet.builder()
				.walk(anotherWalk)
				.pet(defaultPet)
				.build();
		walkPetWithAPDW = WalkPet.builder()
				.walk(defaultWalk)
				.pet(anotherPet)
				.build();
		walkPetWithAPAW = WalkPet.builder()
				.walk(anotherWalk)
				.pet(anotherPet)
				.build();
	}
	
	@Test
	@DisplayName("산책 기록 등록")
	public void register() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.save(any())).thenReturn(defaultWalk);
		when(petRepository.findById(1)).thenReturn(Optional.of(defaultPet));
		when(petRepository.findById(2)).thenReturn(Optional.of(anotherPet));
		WalkRegisterRequestDto requestDto = prepareRegister();
		walkService.register(MEMBER_ID, requestDto);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 산책 기록 등록 실패")
	public void registerNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		WalkRegisterRequestDto requestDto = prepareRegister();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.register(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("등록되지 않은 펫 산책 기록 등록 실패")
	public void registerNotRegisteredPet() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.save(any())).thenReturn(defaultWalk);
		when(petRepository.findById(1)).thenReturn(Optional.of(defaultPet));
		when(petRepository.findById(2)).thenReturn(Optional.empty());
		WalkRegisterRequestDto requestDto = prepareRegister();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.register(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("산책 기록 수정")
	public void modify() {
		// defaultWalk에 defaultPet과 anotherPet에서
		// defaultPet과 theOtherPet으로 산책한 반려동물 목록을 바꾸는 상황
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		when(petRepository.findById(2)).thenReturn(Optional.of(anotherPet));
		when(petRepository.findById(3)).thenReturn(Optional.of(theOtherPet));
		List<WalkPet> walkPetList = new ArrayList<>();
		walkPetList.add(defaultWalkPet);
		walkPetList.add(walkPetWithAPDW);
		when(walkPetRepository.findByWalk(any())).thenReturn(walkPetList);
		when(walkPetRepository.findFirstByWalkAndPet(defaultWalk, anotherPet)).thenReturn(Optional.of(walkPetWithAPDW));
		WalkModifyRequestDto requestDto = prepareModify();
		walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		verify(walkPetRepository, times(1)).delete(any());
		verify(walkPetRepository, times(1)).save(any());
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 산책 기록 수정 실패")
	public void modifyNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		WalkModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 산책 기록 수정 실패")
	public void modifyNotExistWalk() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.findById(anyInt())).thenReturn(Optional.empty());
		WalkModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("수정 권한이 없는 산책 기록 수정 실패")
	public void modifyWalkOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		WalkModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("기존 펫 제거 중 존재하지 않는 펫으로 인해 산책 기록 수정 실패")
	public void modifyOldPetNotExist() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		when(petRepository.findById(2)).thenReturn(Optional.empty());
		List<WalkPet> walkPetList = new ArrayList<>();
		walkPetList.add(defaultWalkPet);
		walkPetList.add(walkPetWithAPDW);
		when(walkPetRepository.findByWalk(any())).thenReturn(walkPetList);
		WalkModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("기존 펫 제거 중 산책 기록 내 존재하지 않는 펫으로 인해 산책 기록 수정 실패")
	public void modifyOldPetNotExistInWalk() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		when(petRepository.findById(2)).thenReturn(Optional.of(anotherPet));
		List<WalkPet> walkPetList = new ArrayList<>();
		walkPetList.add(defaultWalkPet);
		walkPetList.add(walkPetWithAPDW);
		when(walkPetRepository.findByWalk(any())).thenReturn(walkPetList);
		when(walkPetRepository.findFirstByWalkAndPet(defaultWalk, anotherPet)).thenReturn(Optional.empty());
		WalkModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("새 펫 추가 중 존재하지 않는 펫으로 인해 산책 기록 수정 실패")
	public void modifyNewPetNotExist() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		when(petRepository.findById(2)).thenReturn(Optional.of(anotherPet));
		when(petRepository.findById(3)).thenReturn(Optional.empty());
		List<WalkPet> walkPetList = new ArrayList<>();
		walkPetList.add(defaultWalkPet);
		walkPetList.add(walkPetWithAPDW);
		when(walkPetRepository.findByWalk(any())).thenReturn(walkPetList);
		when(walkPetRepository.findFirstByWalkAndPet(defaultWalk, anotherPet)).thenReturn(Optional.of(walkPetWithAPDW));
		WalkModifyRequestDto requestDto = prepareModify();
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.modify(MEMBER_ID, defaultWalk.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("산책 기록 삭제")
	public void delete() {
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		walkService.delete(defaultWalk.getId());
	}
	
	@Test
	@DisplayName("존재하지 않는 산책 기록 삭제 실패")
	public void deleteNotExistWalk() {
		when(walkRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			walkService.delete(defaultWalk.getId());
		});
	}
	
	@Test
	@DisplayName("산책 기록 전체 조회")
	public void listAll() {
		List<Walk> walks = new ArrayList<>();
		walks.add(defaultWalk);
		walks.add(anotherWalk);
		List<WalkPet> defaultWalkPetList = new ArrayList<>();
		List<WalkPet> anotherWalkPetList = new ArrayList<>();
		defaultWalkPetList.add(defaultWalkPet);
		defaultWalkPetList.add(walkPetWithAPDW);
		anotherWalkPetList.add(walkPetWithDPAW);
		anotherWalkPetList.add(walkPetWithAPAW);
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(walkRepository.findByUser(any(), any())).thenReturn(walks);
		when(walkRepository.findById(defaultWalk.getId())).thenReturn(Optional.of(defaultWalk));
		when(walkRepository.findById(anotherWalk.getId())).thenReturn(Optional.of(anotherWalk));
		when(walkPetRepository.findByWalk(defaultWalk)).thenReturn(defaultWalkPetList);
		when(walkPetRepository.findByWalk(anotherWalk)).thenReturn(anotherWalkPetList);
		Page<WalkDetailDto> walkPage = walkService.listAll(MEMBER_ID);
		List<WalkDetailDto> walkList = walkPage.getContent();
		assertEquals(walkList.size(), 2);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 산책 기록 전체 조회 실패")
	public void listAllNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.listAll(MEMBER_ID);
		});
	}
	
	@Test
	@DisplayName("산책 기록 상세 조회")
	public void detail() {
		List<WalkPet> defaultWalkPetList = new ArrayList<>();
		defaultWalkPetList.add(defaultWalkPet);
		defaultWalkPetList.add(walkPetWithAPDW);
		when(walkRepository.findById(anyInt())).thenReturn(Optional.of(defaultWalk));
		when(walkPetRepository.findByWalk(defaultWalk)).thenReturn(defaultWalkPetList);
		WalkDetailDto walk = walkService.detail(defaultWalk.getId());
		assertEquals(walk.getWalk_id(), defaultWalk.getId());
	}
	
	@Test
	@DisplayName("존재하지 않는 산책 기록 상세 조회 실패")
	public void detailNotExistWalk() {
		when(walkRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			walkService.detail(defaultWalk.getId());
		});
	}
	
	@Test
	@DisplayName("반려동물이 오늘 산책을 한 경우 isDone 메소드 true 반환")
	public void isDoneTrue() {
		when(petRepository.findById(anyInt())).thenReturn(Optional.of(defaultPet));
		when(walkPetRepository.findFirstByPetOrderByIdDesc(any())).thenReturn(Optional.of(defaultWalkPet));
		assertTrue(walkService.isDone(defaultPet.getId()));
	}
	
	@Test
	@DisplayName("반려동물이 오늘 산책을 하지 않은 경우 isDone 메소드 false 반환")
	public void isDoneFalse() {
		when(petRepository.findById(anyInt())).thenReturn(Optional.of(defaultPet));
		defaultWalk = Walk.builder()
				.id(1)
				.user(defaultUser)
				.distance(10)
				.start_time(LocalDateTime.now().minusDays(1).minusMinutes(30))
				.end_time(LocalDateTime.now().minusDays(1))
				.build();
		defaultWalkPet = WalkPet.builder()
				.walk(defaultWalk)
				.pet(defaultPet)
				.build();
		when(walkPetRepository.findFirstByPetOrderByIdDesc(any())).thenReturn(Optional.of(defaultWalkPet));
		assertFalse(walkService.isDone(defaultPet.getId()));
	}
	
	@Test
	@DisplayName("반려동물이 존재하지 않은 경우 isDone 메소드 예외 반환")
	public void isDoneNotExistPet() {
		when(petRepository.findById(anyInt())).thenReturn(Optional.of(defaultPet));
		assertThrows(IllegalArgumentException.class, () -> {			
			assertTrue(walkService.isDone(defaultPet.getId()));
		});
	}
	
	@Test
	@DisplayName("반려동물의 산책 기록이 존재하지 않은 경우 isDone 메소드 예외 반환")
	public void isDoneNotExistWalkPet() {
		when(petRepository.findById(anyInt())).thenReturn(Optional.of(defaultPet));
		when(walkPetRepository.findFirstByPetOrderByIdDesc(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			assertTrue(walkService.isDone(defaultPet.getId()));
		});
	}
	
	@Test
	@DisplayName("반려동물의 총 산책 거리와 시간 계산")
	public void walkTimeSum() {
		when(petRepository.findById(anyInt())).thenReturn(Optional.of(defaultPet));
		List<WalkPet> walkPetList = new ArrayList<>();
		walkPetList.add(defaultWalkPet);
		walkPetList.add(walkPetWithDPAW);
		when(walkPetRepository.findByPet(defaultPet)).thenReturn(walkPetList);
		WalkTimeDto walkTimeDto = walkService.walkTimeSum(defaultPet.getId());
		assertEquals(walkTimeDto.getDistance_sum(), defaultWalk.getDistance()+anotherWalk.getDistance());
		long sumOfTime = 0;
		sumOfTime += Duration.between(defaultWalk.getStart_time(), defaultWalk.getEnd_time()).getSeconds();
		sumOfTime += Duration.between(anotherWalk.getStart_time(), anotherWalk.getEnd_time()).getSeconds();		
		assertEquals(walkTimeDto.getTime_passed(), sumOfTime);
	}
	
	@Test
	@DisplayName("존재하지 않는 반려동물의 총 산책 거리와 시간 계산 실패")
	public void walkTimeSumNotExistPet() {
		when(petRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			walkService.walkTimeSum(defaultPet.getId());
		});
	}

	private WalkRegisterRequestDto prepareRegister() {
		List<Integer> pet_ids = new ArrayList<>();
		pet_ids.add(defaultPet.getId());
		pet_ids.add(anotherPet.getId());
		return WalkRegisterRequestDto.builder()
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(30))
				.distance(10)
				.pet_ids(pet_ids)
				.build();
	}
	
	private WalkModifyRequestDto prepareModify() {
		List<Integer> newPetIds = new ArrayList<>();
		newPetIds.add(defaultPet.getId());
		newPetIds.add(theOtherPet.getId());
		return WalkModifyRequestDto.builder()
				.pet_ids(newPetIds)
				.build();
	}
}
