package com.drdoc.BackEnd.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.drdoc.BackEnd.api.domain.Journal;
import com.drdoc.BackEnd.api.domain.User;
import com.drdoc.BackEnd.api.domain.dto.JournalBatchDeleteRequestDto;
import com.drdoc.BackEnd.api.domain.dto.JournalDetailDto;
import com.drdoc.BackEnd.api.domain.dto.JournalRequestDto;
import com.drdoc.BackEnd.api.domain.dto.JournalThumbnailDto;
import com.drdoc.BackEnd.api.repository.JournalRepository;
import com.drdoc.BackEnd.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {
	private final JournalRepository repository;
	private final UserRepository userRepository;

	// 일지 등록
	@Override
	public Journal register(String memberId, JournalRequestDto request) {
		User user = userRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		Journal journal = new Journal(request, user);
		return repository.save(journal);
	}

	// 일지 수정
	@Override
	public Journal modify(String memberId, Integer journalId, JournalRequestDto request) {
		User user = userRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		Journal journal = repository.findById(journalId)
				.orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));
		if (user.getId() != journal.getUser().getId()) {
			throw new AccessDeniedException("권한이 없습니다.");
		}
		journal.modify(request);
		return repository.save(journal);
		
	}

	// 일지 삭제
	@Override
	public void delete(String memberId, int journalId) {
		User user = userRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		Journal journal = repository.findById(journalId)
				.orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다."));
		if (user.getId() != journal.getUser().getId()) {
			throw new AccessDeniedException("권한이 없습니다.");
		}
		repository.deleteById(journalId);
	}

	// 일지 일괄 삭제
	@Override
	public void batchDelete(JournalBatchDeleteRequestDto Journals) {
		repository.deleteAllByIdInBatch(Journals.getJournal_ids());
	}

	// 일지 전체 조회
	@Override
	public Page<JournalThumbnailDto> listAll(String memberId) {
		User user = userRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("가입하지 않은 계정입니다."));
		Sort sort = Sort.by(Sort.Direction.DESC, "id");
		List<JournalThumbnailDto> list = repository.findByUserId(user.getId(), sort).stream()
				.map(JournalThumbnailDto::new).collect(Collectors.toList());
		return new PageImpl<>(list);

	}

	// 일지 상세 조회
	@Override
	public JournalDetailDto detail(int journalId) {
		Journal journal = repository.findById(journalId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
		return new JournalDetailDto(journal);

	}
}
