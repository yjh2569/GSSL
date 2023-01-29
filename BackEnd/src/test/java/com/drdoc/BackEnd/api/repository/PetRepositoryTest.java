package com.drdoc.BackEnd.api.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.drdoc.BackEnd.api.domain.Kind;
import com.drdoc.BackEnd.api.domain.Pet;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.PetModifyRequestDto;

@DataJpaTest
public class PetRepositoryTest {
	
	@Autowired
	private PetRepository petRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PetKindRepository petKindRepository;
	
	private User defaultUser;
	private Pet defaultPet;
	private Kind defaultKind;
	
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
		userRepository.save(defaultUser);
		defaultKind = Kind.builder()
				.name("kindName")
				.build();
		petKindRepository.save(defaultKind);
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
		petRepository.save(defaultPet);
	}
	
	@Test
	@DisplayName("pet 저장")
	public void save() {
		Pet newPet = Pet.builder()
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("test2")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.weight(11.0f)
				.animalPic("testPic2")
				.death(false)
				.diseases("testDisease2")
				.description("testDiscription2")
				.build();
		Pet savedPet = petRepository.save(newPet);
		compareTwoPets(savedPet, newPet);
	}

	@Test
	@DisplayName("한 사용자에 대한 pet 전체 조회")
	public void findAllByUserId() {
		User anotherUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname("testNick2")
				.gender("M")
				.phone("01011112222")
				.build();
		userRepository.save(anotherUser);
		Pet petForAnotherUser = Pet.builder()
				.user(anotherUser)
				.kind(defaultKind)
				.species(true)
				.name("test2")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.death(false)
				.build();
		Pet anotherPetForDefaultUser = Pet.builder()
				.user(defaultUser)
				.kind(defaultKind)
				.species(true)
				.name("test2")
				.gender("F")
				.neutralize(false)
				.birth(LocalDateTime.MIN)
				.death(false)
				.build();
		petRepository.save(petForAnotherUser);
		petRepository.save(anotherPetForDefaultUser);
		List<Pet> petsList = petRepository.findAllByUserId(defaultUser.getId());
		assertEquals(petsList.size(), 2);
		assertEquals(petsList.get(0).getId(), defaultPet.getId());
		assertEquals(petsList.get(1).getId(), anotherPetForDefaultUser.getId());
	}
	
	@Test
	@DisplayName("pet 상세 조회")
	public void findById() {
		Optional<Pet> opPet = petRepository.findById(defaultPet.getId());
		assertTrue(opPet.isPresent());
		Pet petFromRepo = opPet.get();
		compareTwoPets(petFromRepo, defaultPet);
	}
	
	@Test
	@DisplayName("pet 수정")
	public void update() {
		Kind newKind = Kind.builder()
				.name("newKindName")
				.build();
		petKindRepository.save(newKind);
		PetModifyRequestDto requestDto = PetModifyRequestDto.builder()
				.kind_id(newKind.getId())
				.species(false)
				.name("testM")
				.gender("F")
				.neutralize(true)
				.birth(LocalDateTime.MIN.plusSeconds(1))
				.weight(15.0f)
				.animal_pic("testPicM")
				.death(true)
				.diseases("testDiseaseM")
				.description("testDiscriptionM")
				.build();
		defaultPet.modify(requestDto, newKind);
		Pet updatedPet = petRepository.save(defaultPet);
		compareTwoPets(updatedPet, defaultPet);
	}
	
	@Test
	@DisplayName("pet 삭제")
	public void delete() {
		petRepository.delete(defaultPet);
		Optional<Pet> opPet = petRepository.findById(defaultPet.getId());
		assertFalse(opPet.isPresent());
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
