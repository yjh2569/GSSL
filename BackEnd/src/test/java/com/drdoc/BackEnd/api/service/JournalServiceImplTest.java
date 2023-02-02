package com.drdoc.BackEnd.api.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.springframework.security.access.AccessDeniedException;

import com.drdoc.BackEnd.api.domain.Journal;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.JournalDetailDto;
import com.drdoc.BackEnd.api.domain.dto.JournalRequestDto;
import com.drdoc.BackEnd.api.domain.dto.JournalThumbnailDto;
import com.drdoc.BackEnd.api.repository.JournalRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class JournalServiceImplTest {
	
	@Mock
	JournalRepository journalRepository;
	
	@Mock
	UserRepository userRepository;
	
	@InjectMocks
	JournalServiceImpl journalService;
	
	private User defaultUser;
	private User anotherUser;
	private Journal defaultJournal;
	
	private final String MEMBER_ID = "test";
	
	@BeforeEach
	public void setUp() {
		defaultUser = User.builder()
				.memberId(MEMBER_ID)
				.email("test@test.com")
				.password("password")
				.nickname("testNick")
				.gender("M")
				.phone("01012345678")
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
		defaultJournal = Journal.builder()
				.user(defaultUser)
				.petId(1)
				.picture("testPicture")
				.part("test")
				.symptom("test")
				.result("test")
				.build();
	}
	
	@Test
	@DisplayName("일지 작성")
	public void register() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(journalRepository.save(any())).thenReturn(defaultJournal);
		JournalRequestDto requestDto = prepareRequest();
		Journal savedJournal = journalService.register(MEMBER_ID, requestDto);
		compareTwoJournal(savedJournal, defaultJournal);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자의 일지 작성 실패")
	public void registerNotRegistedUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		JournalRequestDto requestDto = prepareRequest();
		assertThrows(IllegalArgumentException.class, () -> {			
			journalService.register(MEMBER_ID, requestDto);
		});
	}
	
	@Test
	@DisplayName("일지 수정")
	public void modify() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(journalRepository.findById(anyInt())).thenReturn(Optional.of(defaultJournal));
		when(journalRepository.save(any())).thenReturn(defaultJournal);
		JournalRequestDto requestDto = prepareRequest();
		Journal modifiedJournal = journalService.modify(MEMBER_ID, defaultJournal.getId(), requestDto);
		compareTwoJournal(modifiedJournal, defaultJournal);
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 일지 수정 실패")
	public void modifyNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		JournalRequestDto requestDto = prepareRequest();
		assertThrows(IllegalArgumentException.class, () -> {
			journalService.modify(MEMBER_ID, defaultJournal.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 일지 수정 실패")
	public void modifyNotExistJournal() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(journalRepository.findById(anyInt())).thenReturn(Optional.empty());
		JournalRequestDto requestDto = prepareRequest();
		assertThrows(IllegalArgumentException.class, () -> {
			journalService.modify(MEMBER_ID, defaultJournal.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("수정 권한이 없는 일지 수정 실패")
	public void modifyJournalOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(journalRepository.findById(anyInt())).thenReturn(Optional.of(defaultJournal));
		JournalRequestDto requestDto = prepareRequest();
		assertThrows(AccessDeniedException.class, () -> {
			journalService.modify(MEMBER_ID, defaultJournal.getId(), requestDto);
		});
	}
	
	@Test
	@DisplayName("일지 삭제")
	public void delete() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(journalRepository.findById(anyInt())).thenReturn(Optional.of(defaultJournal));
		journalService.delete(MEMBER_ID, defaultJournal.getId());
	}
	
	@Test
	@DisplayName("가입하지 않은 사용자 일지 삭제 실패")
	public void deleteNotRegisteredUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			journalService.delete(MEMBER_ID, defaultJournal.getId());
		});
	}
	
	@Test
	@DisplayName("존재하지 않는 일지 삭제 실패")
	public void deleteNotExistJournal() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(defaultUser));
		when(journalRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {
			journalService.delete(MEMBER_ID, defaultJournal.getId());
		});
	}
	
	@Test
	@DisplayName("삭제 권한이 없는 일지 삭제 실패")
	public void deleteJournalOfAnotherUser() {
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(journalRepository.findById(anyInt())).thenReturn(Optional.of(defaultJournal));
		assertThrows(AccessDeniedException.class, () -> {
			journalService.delete(MEMBER_ID, defaultJournal.getId());
		});
	}
	
	@Test
	@DisplayName("일지 리스트 조회")
	public void listAll() {
		List<Journal> journals = new ArrayList<>();
		Journal journal1 = Journal.builder()
				.user(defaultUser)
				.petId(1)
				.picture("testPicture1")
				.part("test1")
				.symptom("test1")
				.result("test1")
				.build();
		Journal journal2 = Journal.builder()
				.user(defaultUser)
				.petId(1)
				.picture("testPicture2")
				.part("test2")
				.symptom("test2")
				.result("test2")
				.build();
		journals.add(defaultJournal);
		journals.add(journal1);
		journals.add(journal2);
		when(userRepository.findByMemberId(any())).thenReturn(Optional.of(anotherUser));
		when(journalRepository.findByUserId(anyInt(), any())).thenReturn(journals);
		Page<JournalThumbnailDto> journalPage = journalService.listAll(MEMBER_ID);
		List<JournalThumbnailDto> journalList = journalPage.getContent();
		assertEquals(journalList.size(), 3);
	}
	
	@Test
	@DisplayName("일지 상세 조회")
	public void detail() {
		when(journalRepository.findById(anyInt())).thenReturn(Optional.of(defaultJournal));
		JournalDetailDto journal = journalService.detail(defaultJournal.getId());
		assertEquals(journal.getId(), defaultJournal.getId());
	}
	
	@Test
	@DisplayName("존재하지 않는 일지 상세 조회 실패")
	public void detailNotExistJournal() {
		when(journalRepository.findById(anyInt())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> {			
			journalService.detail(defaultJournal.getId());
		});
	}

	private JournalRequestDto prepareRequest() {
		return JournalRequestDto.builder()
				.pet_id(1)
				.picture("testPicture1")
				.part("test1")
				.symptom("test1")
				.result("test1")
				.build();
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
