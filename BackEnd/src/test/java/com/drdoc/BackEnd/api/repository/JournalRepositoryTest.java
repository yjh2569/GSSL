package com.drdoc.BackEnd.api.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import com.drdoc.BackEnd.api.domain.Journal;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.JournalRequestDto;

@DataJpaTest
public class JournalRepositoryTest {
	
	@Autowired
	private JournalRepository journalRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	private User defaultUser;
	private Journal defaultJournal;
	
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
		defaultJournal = Journal.builder()
				.user(defaultUser)
				.petId(1)
				.picture("testPicture")
				.part("test")
				.symptom("test")
				.result("test")
				.build();
		journalRepository.save(defaultJournal);
	}
	
	@Test
	@DisplayName("journal 저장")
	public void save() {
		Journal newJournal = Journal.builder()
				.user(defaultUser)
				.petId(1)
				.picture("testPictureN")
				.part("testN")
				.symptom("testN")
				.result("testN")
				.build();
		Journal savedJournal = journalRepository.save(newJournal);
		compareTwoJournal(newJournal, savedJournal);
	}
	
	@Test
	@DisplayName("user의 journal 리스트 조회")
	public void findByUserId() {
		User anotherUser = User.builder()
				.memberId("test2")
				.email("test2@test.com")
				.password("password2")
				.nickname("testNick2")
				.gender("M")
				.phone("01011112222")
				.build();
		userRepository.save(anotherUser);
		Journal journalForAnotherUser = Journal.builder()
				.user(anotherUser)
				.petId(2)
				.picture("testPictureA")
				.part("testA")
				.symptom("testA")
				.result("testA")
				.build();
		journalRepository.saveAndFlush(journalForAnotherUser);
		Journal anotherJournalForDefaultUser = Journal.builder()
				.user(defaultUser)
				.petId(3)
				.picture("testPictureB")
				.part("testB")
				.symptom("testB")
				.result("testB")
				.build();
		journalRepository.saveAndFlush(anotherJournalForDefaultUser);
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		List<Journal> journals = journalRepository.findByUserId(defaultUser.getId(), sort);
		assertEquals(journals.size(), 2);
		compareTwoJournal(journals.get(0), anotherJournalForDefaultUser);
		compareTwoJournal(journals.get(1), defaultJournal);
	}
	
	@Test
	@DisplayName("journal 상세 조회")
	public void findById() {
		Optional<Journal> opJournal = journalRepository.findById(defaultJournal.getId());
		assertTrue(opJournal.isPresent());
		Journal journalFromRepo = opJournal.get();
		compareTwoJournal(journalFromRepo, defaultJournal);
	}
	
	@Test
	@DisplayName("journal 수정")
	public void update() {
		JournalRequestDto requestDto = JournalRequestDto.builder()
				.pet_id(2)
				.picture("testU")
				.part("testU")
				.symptom("testU")
				.result("testU")
				.build();
		defaultJournal.modify(requestDto);
		Journal updatedJournal = journalRepository.save(defaultJournal);
		compareTwoJournal(updatedJournal, defaultJournal);
	}
	
	@Test
	@DisplayName("journal 삭제")
	public void delete() {
		journalRepository.delete(defaultJournal);
		Optional<Journal> opJournal = journalRepository.findById(defaultJournal.getId());
		assertFalse(opJournal.isPresent());
	}
	
	@Test
	@DisplayName("한 pet의 journal 모두 삭제")
	public void deleteByPetId() {
		Journal anotherJournalForDefaultPet = Journal.builder()
				.user(defaultUser)
				.petId(1)
				.picture("testPictureA")
				.part("testA")
				.symptom("testA")
				.result("testA")
				.build();
		journalRepository.saveAndFlush(anotherJournalForDefaultPet);
		Journal journalForAnotherPet = Journal.builder()
				.user(defaultUser)
				.petId(2)
				.picture("testPictureB")
				.part("testB")
				.symptom("testB")
				.result("testB")
				.build();
		journalRepository.saveAndFlush(journalForAnotherPet);
		journalRepository.deleteByPetId(1);
		List<Journal> journals = journalRepository.findAll();
		assertEquals(journals.size(), 1);
		compareTwoJournal(journals.get(0), journalForAnotherPet);
	}

	private void compareTwoJournal(Journal journal1, Journal journal2) {
		assertEquals(journal1.getId(), journal2.getId());
		assertEquals(journal1.getUser().getId(), journal2.getUser().getId());
		assertEquals(journal1.getPetId(), journal2.getPetId());
		assertEquals(journal1.getPicture(), journal2.getPicture());
		assertEquals(journal1.getPart(), journal2.getPart());
		assertEquals(journal1.getSymptom(), journal2.getSymptom());
		assertEquals(journal1.getResult(), journal2.getResult());
	}
}
