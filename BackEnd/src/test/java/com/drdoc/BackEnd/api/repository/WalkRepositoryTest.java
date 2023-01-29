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
import org.springframework.data.domain.Sort;

import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.Walk;
import com.drdoc.BackEnd.api.domain.dto.WalkRegisterRequestDto;

@DataJpaTest
public class WalkRepositoryTest {
	
	@Autowired
	private WalkRepository walkRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	private User defaultUser;
	private Walk defaultWalk;
	
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
		defaultWalk = Walk.builder()
				.user(defaultUser)
				.distance(10)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(30))
				.build();
		walkRepository.save(defaultWalk);
	}
	
	@Test
	@DisplayName("walk 저장")
	public void save() {
		Walk newWalk = Walk.builder()
				.user(defaultUser)
				.distance(20)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(50))
				.build();
		Walk savedWalk = walkRepository.save(newWalk);
		compareTwoWalks(savedWalk, newWalk);
	}
	
	@Test
	@DisplayName("한 user에 대한 walk 리스트 조회")
	public void findByUser() {
		User anotherUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname("testNick2")
				.gender("F")
				.phone("0101111222")
				.build();
		userRepository.saveAndFlush(anotherUser);
		Walk walkForAnotherUser = Walk.builder()
				.user(anotherUser)
				.distance(12)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(20))
				.build();
		walkRepository.saveAndFlush(walkForAnotherUser);
		Walk anotherWalkForDefaultUser = Walk.builder()
				.user(defaultUser)
				.distance(16)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(40))
				.build();
		walkRepository.saveAndFlush(anotherWalkForDefaultUser);
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		List<Walk> walks = walkRepository.findByUser(defaultUser, sort);
		assertEquals(walks.size(), 2);
		compareTwoWalks(walks.get(0), anotherWalkForDefaultUser);
		compareTwoWalks(walks.get(1), defaultWalk);		
	}
	
	@Test
	@DisplayName("walk 상세 조회")
	public void findById() {
		Optional<Walk> opWalk = walkRepository.findById(defaultWalk.getId());
		assertTrue(opWalk.isPresent());
		Walk walkFromRepo = opWalk.get();
		compareTwoWalks(walkFromRepo, defaultWalk);
	}
	
	@Test
	@DisplayName("walk 수정")
	public void update() {
		WalkRegisterRequestDto requestDto = WalkRegisterRequestDto.builder()
				.distance(15)
				.start_time(LocalDateTime.now())
				.end_time(LocalDateTime.now().plusMinutes(40))
				.build();
		defaultWalk.modify(requestDto);
		Walk updatedWalk = walkRepository.save(defaultWalk);
		compareTwoWalks(updatedWalk, defaultWalk);
	}
	
	@Test
	@DisplayName("walk 삭제")
	public void delete() {
		walkRepository.delete(defaultWalk);
		Optional<Walk> opWalk = walkRepository.findById(defaultWalk.getId());
		assertFalse(opWalk.isPresent());
	}

	private void compareTwoWalks(Walk walk1, Walk walk2) {
		assertEquals(walk1.getId(), walk2.getId());
		assertEquals(walk1.getUser().getId(), walk2.getUser().getId());
		assertEquals(walk1.getDistance(), walk2.getDistance());
		assertEquals(walk1.getStart_time(), walk2.getStart_time());
		assertEquals(walk1.getEnd_time(), walk2.getEnd_time());
	}
}
