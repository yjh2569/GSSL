package com.drdoc.BackEnd.api.jwt;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.drdoc.BackEnd.api.domain.dto.TokenDto;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = { "classpath:application.yml" })
public class JwtFilterTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	TokenProvider tokenProvider;

	private static final String BEARER_TYPE = "Bearer ";

	/**
	 * AccessToken : 존재하지 않음
	 */
	@Test
	@DisplayName("AccessToken 없이 API 사용 시 Unauthorized")
	public void noAccessToken() throws Exception {
		mvc.perform(get("/api/user")).andExpect(status().isUnauthorized());
	}

	/**
	 * AccessToken : 유효
	 */
	@Test
	@DisplayName("유효한 AccessToken를 소유한 채 API 사용 시 정상 호출 확인")
	public void validAccessToken() throws Exception {
		String memberId = "testMemberId";

		TokenDto token = tokenProvider.generateTokenDto(memberId);

		assertNotNull(token.getAccessToken());
		mvc.perform(get("/api/user").header("Authorization", 
				BEARER_TYPE + token.getAccessToken()))
				.andExpect(status().isOk());
	}

	/**
	 * AccessToken : 유효하지 않음
	 */
	@Test
	@DisplayName("유효하지 않은 AccessToken를 소유한 채 API 사용 시 Unauthorized 확인")
	public void invalidAccessToken() throws Exception {
		String memberId = "testMemberId";

		TokenDto token = tokenProvider.generateTokenDto(memberId);

		assertNotNull(token.getAccessToken());
		mvc.perform(get("/api/user").header("Authorization", 
				BEARER_TYPE + token.getAccessToken() + "invalid_string"))
				.andExpect(status().isUnauthorized());
	}
}
