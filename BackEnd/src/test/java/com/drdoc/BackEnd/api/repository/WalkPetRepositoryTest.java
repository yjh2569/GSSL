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
import com.drdoc.BackEnd.api.domain.Walk;
import com.drdoc.BackEnd.api.domain.WalkPet;

@DataJpaTest
public class WalkPetRepositoryTest {
	
	@Autowired
	private WalkPetRepository walkPetRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private WalkRepository walkRepository;
	
	@Autowired
	private PetRepository petRepository;
	
	@Autowired
	private PetKindRepository petKindRepository;
	
	private User defaultUser;
	private Pet defaultPet;
	private Pet anotherPet;
	private Kind defaultKind;
	private Walk defaultWalk;
	private Walk anotherWalk;
	private WalkPet defaultWalkPet;
	private WalkPet walkPetWithDPAW;
	private WalkPet walkPetWithAPDW;
	private WalkPet walkPetWithAPAW;
	
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
		anotherPet = Pet.builder()
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
		petRepository.save(anotherPet);
		defaultWalk = Walk.builder()
				.user(defaultUser)
				.distance(10)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(30))
				.build();
		walkRepository.save(defaultWalk);
		anotherWalk = Walk.builder()
				.user(defaultUser)
				.distance(12)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(20))
				.build();
		walkRepository.save(anotherWalk);
		defaultWalkPet = WalkPet.builder()
				.walk(defaultWalk)
				.pet(defaultPet)
				.build();
		walkPetRepository.save(defaultWalkPet);
		walkPetWithDPAW = WalkPet.builder()
				.walk(anotherWalk)
				.pet(defaultPet)
				.build();
		walkPetRepository.save(walkPetWithDPAW);
		walkPetWithAPDW = WalkPet.builder()
				.walk(defaultWalk)
				.pet(anotherPet)
				.build();
		walkPetRepository.save(walkPetWithAPDW);
		walkPetWithAPAW = WalkPet.builder()
				.walk(anotherWalk)
				.pet(anotherPet)
				.build();
		walkPetRepository.save(walkPetWithAPAW);
	}
	
	@Test
	@DisplayName("walkPet 저장")
	public void save() {
		WalkPet newWalkPet = WalkPet.builder()
				.walk(defaultWalk)
				.pet(defaultPet)
				.build();
		WalkPet savedWalkPet = walkPetRepository.save(newWalkPet);
		compareTwoWalkPets(savedWalkPet, newWalkPet);
	}
	
	@Test
	@DisplayName("walk에 포함된 walkPet 리스트 조회")
	public void findByWalk() {
		List<WalkPet> walkPets = walkPetRepository.findByWalk(defaultWalk);
		assertEquals(walkPets.size(), 2);
		compareTwoWalkPets(walkPets.get(0), defaultWalkPet);
		compareTwoWalkPets(walkPets.get(1), walkPetWithAPDW);
	}
	
	@Test
	@DisplayName("pet이 포함된 walkPet 리스트 조회")
	public void findByPet() {
		List<WalkPet> walkPets = walkPetRepository.findByPet(defaultPet);
		assertEquals(walkPets.size(), 2);
		compareTwoWalkPets(walkPets.get(0), defaultWalkPet);
		compareTwoWalkPets(walkPets.get(1), walkPetWithDPAW);
	}
	
	@Test
	@DisplayName("walk와 pet을 활용한 walkPet 조회")
	public void findByWalkAndPet() {
		Optional<WalkPet> opWalkPet = walkPetRepository.findFirstByWalkAndPet(defaultWalk, defaultPet);
		assertTrue(opWalkPet.isPresent());
		WalkPet walkPetFromRepo = opWalkPet.get();
		compareTwoWalkPets(walkPetFromRepo, defaultWalkPet);
	}
	
	@Test
	@DisplayName("pet의 가장 최근 walkPet 조회")
	public void findFirstByPetOrderByIdDesc() {
		Optional<WalkPet> opWalkPet = walkPetRepository.findFirstByPetOrderByIdDesc(defaultPet);
		assertTrue(opWalkPet.isPresent());
		WalkPet walkPetFromRepo = opWalkPet.get();
		compareTwoWalkPets(walkPetFromRepo, walkPetWithDPAW);
	}
	
	@Test
	@DisplayName("walkPet 삭제")
	public void delete() {
		walkPetRepository.delete(defaultWalkPet);
		Optional<WalkPet> opWalkPet = walkPetRepository.findById(defaultWalkPet.getId());
		assertFalse(opWalkPet.isPresent());
	}

	private void compareTwoWalkPets(WalkPet walkPet1, WalkPet walkPet2) {
		assertEquals(walkPet1.getId(), walkPet2.getId());
		assertEquals(walkPet1.getWalk().getId(), walkPet2.getWalk().getId());
		assertEquals(walkPet1.getPet().getId(), walkPet2.getPet().getId());
	}
}
