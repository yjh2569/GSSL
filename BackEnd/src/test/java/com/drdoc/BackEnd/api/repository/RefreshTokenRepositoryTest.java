package com.drdoc.BackEnd.api.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.drdoc.BackEnd.api.domain.RefreshToken;
import com.drdoc.BackEnd.api.jwt.TokenProvider;

@DataJpaTest
public class RefreshTokenRepositoryTest {
	
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	
	private RefreshToken defaultRefreshToken;
	private final String DEFAULT_KEY = "testKey";
	private final String DEFAULT_VALUE = "testValue";
	private LocalDateTime expiredTime;
	
	@BeforeEach
	public void setUp() {
		defaultRefreshToken = RefreshToken.builder()
				.key(DEFAULT_KEY)
				.value(DEFAULT_VALUE)
				.expireTime(LocalDateTime.MAX)
				.build();
		refreshTokenRepository.save(defaultRefreshToken);
		expiredTime = defaultRefreshToken.getExpireTime();
	}
	
	@Test
	@DisplayName("refresh token 저장")
	public void save() {
		RefreshToken newRefreshToken = RefreshToken.builder()
				.key("testKey2")
				.value("testValue2")
				.expireTime(LocalDateTime.MAX)
				.build();
		RefreshToken savedRefreshToken = refreshTokenRepository.save(newRefreshToken);
		assertEquals(savedRefreshToken.getId(), newRefreshToken.getId());
		assertEquals(savedRefreshToken.getKey(), newRefreshToken.getKey());
		assertEquals(savedRefreshToken.getValue(), newRefreshToken.getValue());
		assertTrue(savedRefreshToken.getExpireTime()
				.isBefore(LocalDateTime.now()
						.plusSeconds(TokenProvider.REFRESH_TOKEN_EXPIRE_TIME / 1000 + 1)));
	}
	
	@Test
	@DisplayName("key를 이용한 refresh token 조회")
	public void findByKey() {
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository.findByKey(DEFAULT_KEY);
		assertTrue(opRefreshToken.isPresent());
		RefreshToken refreshTokenFromRepo = opRefreshToken.get();
		assertEquals(refreshTokenFromRepo.getId(), defaultRefreshToken.getId());
		assertEquals(refreshTokenFromRepo.getKey(), DEFAULT_KEY);
		assertEquals(refreshTokenFromRepo.getValue(), DEFAULT_VALUE);
	}
	
	@Test
	@DisplayName("잘못된 key를 이용한 refresh token 조회 실패")
	public void findByWrongKey() {
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository.findByKey("wrongKey");
		assertFalse(opRefreshToken.isPresent());
	}
	
	@Test
	@DisplayName("value를 이용한 refresh token 조회")
	public void findByValue() {
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository.findByValue(DEFAULT_VALUE);
		assertTrue(opRefreshToken.isPresent());
		RefreshToken refreshTokenFromRepo = opRefreshToken.get();
		assertEquals(refreshTokenFromRepo.getId(), defaultRefreshToken.getId());
		assertEquals(refreshTokenFromRepo.getKey(), DEFAULT_KEY);
		assertEquals(refreshTokenFromRepo.getValue(), DEFAULT_VALUE);
	}
	
	@Test
	@DisplayName("잘못된 value를 이용한 refresh token 조회 실패")
	public void findByWrongValue() {
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository.findByValue("wrongValue");
		assertFalse(opRefreshToken.isPresent());
	}
	
	@Test
	@DisplayName("만료 시간이 지난 refresh token 삭제")
	public void deleteByExpireTimeLessThan() {
		LocalDateTime nowForTest = expiredTime.plusSeconds(1);
		refreshTokenRepository.deleteByExpireTimeLessThan(nowForTest);
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository
				.findById(defaultRefreshToken.getId());
		assertFalse(opRefreshToken.isPresent());
	}
	
	@Test
	@DisplayName("만료 시간이 지나지 않은 refresh token 미삭제")
	public void notDeleteByEnoughExpireTime() {
		LocalDateTime nowForTest = expiredTime.minusSeconds(1);
		refreshTokenRepository.deleteByExpireTimeLessThan(nowForTest);
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository
				.findById(defaultRefreshToken.getId());
		assertTrue(opRefreshToken.isPresent());
		RefreshToken refreshTokenFromRepo = opRefreshToken.get();
		assertEquals(refreshTokenFromRepo.getId(), defaultRefreshToken.getId());
	}
	
	@Test
	@DisplayName("value를 이용한 refresh token 삭제")
	public void deleteByValue() {
		refreshTokenRepository.deleteByValue(DEFAULT_VALUE);
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository
				.findById(defaultRefreshToken.getId());
		assertFalse(opRefreshToken.isPresent());
	}
	
	@Test
	@DisplayName("value를 이용한 refresh token 삭제")
	public void deleteByWrongValue() {
		refreshTokenRepository.deleteByValue("wrongValue");
		Optional<RefreshToken> opRefreshToken = refreshTokenRepository
				.findById(defaultRefreshToken.getId());
		assertTrue(opRefreshToken.isPresent());
		RefreshToken refreshTokenFromRepo = opRefreshToken.get();
		assertEquals(refreshTokenFromRepo.getId(), defaultRefreshToken.getId());
	}
	
	@Test
	@DisplayName("refresh token 수정")
	public void update() {
		String newToken = "newToken";
		defaultRefreshToken.updateValue(newToken);
		RefreshToken updatedRefreshToken = refreshTokenRepository.save(defaultRefreshToken);
		assertEquals(updatedRefreshToken.getId(), defaultRefreshToken.getId());
		assertEquals(updatedRefreshToken.getKey(), DEFAULT_KEY);
		assertEquals(updatedRefreshToken.getValue(), newToken);
		assertTrue(updatedRefreshToken.getExpireTime().isAfter(expiredTime));
		assertTrue(updatedRefreshToken.getExpireTime()
				.isBefore(LocalDateTime.now()
						.plusSeconds(TokenProvider.REFRESH_TOKEN_EXPIRE_TIME / 1000 + 1)));
	}
}
