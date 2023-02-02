package com.drdoc.BackEnd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.springframework.security.access.AccessDeniedException;

import com.drdoc.BackEnd.api.domain.Kind;
import com.drdoc.BackEnd.api.domain.Pet;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.PetDetailDto;
import com.drdoc.BackEnd.api.domain.dto.PetKindListDto;
import com.drdoc.BackEnd.api.domain.dto.PetListDto;
import com.drdoc.BackEnd.api.domain.dto.PetModifyRequestDto;
import com.drdoc.BackEnd.api.domain.dto.PetRegisterRequestDto;
import com.drdoc.BackEnd.api.repository.JournalRepository;
import com.drdoc.BackEnd.api.repository.PetKindRepository;
import com.drdoc.BackEnd.api.repository.PetRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PetServiceImplTest {
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private PetRepository petRepository;
	
	@Mock
	private PetKindRepository petKindRepository;
	
	@Mock
	private JournalRepository journalRepository;
	
	@InjectMocks
	private PetServiceImpl petService;
	
	private User defaultUser;
	private User anotherUser;
	private Pet defaultPet;
	private Kind defaultKind;
	
	private final String MEMBER_ID = "test";
	
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
				.profilePic("testPic")
				.build();
		anotherUser = User.builder()
				.id(2)
				.memberId("test2")
				.email("test@test.com")
				.password("password")
				.nickname("testNick2")
				.gender("M")
				.phone("01012345678")
				.build();
		defaultKind = Kind.builder()
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
	@DisplayName("펫 등록")
	public void registerPet() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(petKindRepository.findById(any())).thenReturn(Optional.of(defaultKind));
		when(petRepository.save(any())).thenReturn(defaultPet);
		PetRegisterRequestDto requestDto = prepareRegisterPet();
		Pet registeredPet = petService.registerPet(MEMBER_ID, requestDto);
		compareTwoPets(registeredPet, defaultPet);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 펫 등록 실패")
	public void registerPetNotRegisteredUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		PetRegisterRequestDto requestDto = prepareRegisterPet();
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.registerPet(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("품종 번호가 올바르지 않은 펫 등록 실패")
	public void registerPetNotRegisteredPetKind() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(petKindRepository.findById(any())).thenReturn(Optional.empty());
		PetRegisterRequestDto requestDto = prepareRegisterPet();
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.registerPet(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("펫 수정")
	public void modifyPet() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(petKindRepository.findById(any())).thenReturn(Optional.of(defaultKind));
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		when(petRepository.save(any())).thenReturn(defaultPet);
		PetModifyRequestDto requestDto = prepareModifyPet();
		Pet modifiedPet = petService.modifyPet(MEMBER_ID, defaultPet.getId(), requestDto);
		compareTwoPets(modifiedPet, defaultPet);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 펫 수정 실패")
	public void modifyPetNotRegisterdUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		PetModifyRequestDto requestDto = prepareModifyPet();
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.modifyPet(MEMBER_ID, defaultPet.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 펫 수정 실패")
	public void modifyPetNotExist() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(petRepository.findById(any())).thenReturn(Optional.empty());
		PetModifyRequestDto requestDto = prepareModifyPet();
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.modifyPet(MEMBER_ID, defaultPet.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("품종 번호가 올바르지 않은 펫 수정 실패")
	public void modifyPetNotRegisteredPetKind() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(petKindRepository.findById(any())).thenReturn(Optional.empty());
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		PetModifyRequestDto requestDto = prepareModifyPet();
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.modifyPet(MEMBER_ID, defaultPet.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("사용자의 펫이 아니라서 수정 권한 없는 펫 수정 실패")
	public void modifyPetOfAnotherUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(anotherUser));
		when(petKindRepository.findById(any())).thenReturn(Optional.of(defaultKind));
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		PetModifyRequestDto requestDto = prepareModifyPet();
		assertThrows(AccessDeniedException.class, () -> {			
			petService.modifyPet(MEMBER_ID, defaultPet.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("펫 사진 경로 조회")
	public void getPetImage() {
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		String petImage = petService.getPetImage(defaultPet.getId());
		assertEquals(petImage, defaultPet.getAnimalPic());
	}
	
	@Test
	@DisplayName("존재하지 않는 펫 사진 경로 조회 실패")
	public void getNotExistPetImage() {
		when(petRepository.findById(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			petService.getPetImage(defaultPet.getId());
		});
	}
	
	@Test
	@DisplayName("펫 삭제")
	public void deletePet() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		petService.deletePet(MEMBER_ID, defaultPet.getId());
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 펫 삭제 실패")
	public void deletePetNotRegisteredUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.deletePet(MEMBER_ID, defaultPet.getId());
		});
	}
	
	@Test
	@DisplayName("다른 사용자의 펫 삭제 실패")
	public void deletePetOfAnotherUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(anotherUser));
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		assertThrows(AccessDeniedException.class, () -> {			
			petService.deletePet(MEMBER_ID, defaultPet.getId());
		});
	}
	
	@Test
	@DisplayName("사용자의 펫 목록 조회")
	public void getPetList() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		List<Pet> pets = new ArrayList<>();
		pets.add(defaultPet);
		Pet pet1 = Pet.builder()
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("test1")
				.gender("M")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(11.0f)
				.animalPic("testPic1")
				.death(false)
				.diseases("testDisease1")
				.description("testDiscription1")
				.build();
		pets.add(pet1);
		Pet pet2 = Pet.builder()
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("test2")
				.gender("M")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(12.0f)
				.animalPic("testPic2")
				.death(false)
				.diseases("testDisease2")
				.description("testDiscription2")
				.build();
		pets.add(pet2);
		when(petRepository.findAllByUserId(defaultUser.getId())).thenReturn(pets);
		List<PetListDto> petList = petService.getPetList(MEMBER_ID);
		assertEquals(petList.size(), pets.size());
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 펫 목록 조회 실패")
	public void getPetListOfNotRegisteredUser() {
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			petService.getPetList(MEMBER_ID);			
		});
	}
	
	@Test
	@DisplayName("탈퇴한 사용자의 펫 목록 조회 실패")
	public void getPetListOfQuittedUser() {
		defaultUser.quit();
		when(userRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(defaultUser));
		assertThrows(IllegalArgumentException.class, () -> {
			petService.getPetList(MEMBER_ID);			
		});
	}
	
	@Test
	@DisplayName("펫 상세 조회")
	public void getPetDetail() {
		when(petRepository.findById(any())).thenReturn(Optional.of(defaultPet));
		PetDetailDto pet = petService.getPetDetail(defaultPet.getId());
		assertEquals(pet.getId(), defaultPet.getId());
		assertEquals(pet.getName(), defaultPet.getName());
	}
	
	@Test
	@DisplayName("존재하지 않는 펫 상세 조회 실패")
	public void getPetDetailNotExist() {
		when(petRepository.findById(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			petService.getPetDetail(defaultPet.getId());
		});
	}
	
	@Test
	@DisplayName("품종 리스트 조회")
	public void getPetKindList() {
		List<Kind> petKinds = new ArrayList<>();
		petKinds.add(defaultKind);
		Kind kind1 = Kind.builder()
				.name("kind1")
				.build();
		petKinds.add(kind1);
		Kind kind2 = Kind.builder()
				.name("kind2")
				.build();
		petKinds.add(kind2);
		when(petKindRepository.findAll()).thenReturn(petKinds);
		List<PetKindListDto> petKindList = petService.getPetKindList();
		assertEquals(petKindList.size(), petKinds.size());		
	}
	
	@Test
	@DisplayName("품종 상세 조회")
	public void getPetKind() {
		when(petKindRepository.findById(any())).thenReturn(Optional.of(defaultKind));
		PetKindListDto petKind = petService.getPetKind(defaultKind.getId());
		assertEquals(petKind.getName(), defaultKind.getName());
	}
	
	@Test
	@DisplayName("존재하지 않는 품종 상세 조회 실패")
	public void getPetKindNotExist() {
		when(petKindRepository.findById(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			petService.getPetKind(defaultKind.getId());
		});
	}
	
	private PetModifyRequestDto prepareModifyPet() {
		return PetModifyRequestDto.builder()
				.kind_id(defaultKind.getId())
				.species(true)
				.name("test")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(11.0f)
				.animal_pic("testPic")
				.death(false)
				.diseases("testDisease")
				.description("testDiscription")
				.build();
	}

	private PetRegisterRequestDto prepareRegisterPet() {
		return PetRegisterRequestDto.builder()
				.kind_id(defaultKind.getId())
				.species(true)
				.name("test")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(11.0f)
				.animal_pic("testPic")
				.death(false)
				.diseases("testDisease")
				.description("testDiscription")
				.build();
	}
	
	private void compareTwoPets(Pet pet1, Pet pet2) {
		assertEquals(pet1.getUser().getId(), pet2.getUser().getId());
		assertEquals(pet1.getKind().getId(), pet2.getKind().getId());
		assertEquals(pet1.isSpecies(), pet2.isSpecies());
		assertEquals(pet1.getName(), pet2.getName());
		assertEquals(pet1.getGender(), pet2.getGender());
		assertEquals(pet1.isNeutralize(), pet2.isNeutralize());
		assertEquals(pet1.getBirth(), pet2.getBirth());
		assertEquals(pet1.getWeight(), pet2.getWeight());
		assertEquals(pet1.getAnimalPic(), pet2.getAnimalPic());
		assertEquals(pet1.isDeath(), pet2.isDeath());
		assertEquals(pet1.getDiseases(), pet2.getDiseases());
		assertEquals(pet1.getDescription(), pet2.getDescription());
	}
}
