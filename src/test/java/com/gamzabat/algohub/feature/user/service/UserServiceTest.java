package com.gamzabat.algohub.feature.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.gamzabat.algohub.feature.user.exception.BOJServerErrorException;
import com.gamzabat.algohub.feature.user.exception.CheckBjNicknameValidationException;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@InjectMocks
	private UserService userService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private RestTemplate restTemplate;

	@Test
	@DisplayName("백준 닉네임 유효성 검증 : 사용 가능한 백준 닉네임")
	void checkBjNickname_1() {
		// given
		String bjNickname = "bjNickname";
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
			.thenReturn(new ResponseEntity<>(HttpStatus.OK));
		// when(userRepository.existsByBjNickname(bjNickname)).thenReturn(false);
		// when
		userService.checkBjNickname(bjNickname);
		// then
		// verify(userRepository, times(1)).existsByBjNickname(bjNickname);
	}

	@Test
	@DisplayName("백준 닉네임 유효성 검증 : 유효하지 않은 백준 닉네임")
	void checkBjNickname_2() {
		// given
		String bjNickname = "bjNickname";
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
			.thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		// when, then
		assertThatThrownBy(() -> userService.checkBjNickname(bjNickname))
			.isInstanceOf(CheckBjNicknameValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "백준 닉네임이 유효하지 않습니다.");
	}

	// @Test
	// @DisplayName("백준 닉네임 유효성 검증 : 이미 가입된 백준 닉네임")
	// void checkBjNickname_3() {
	// 	// given
	// 	String bjNickname = "bjNickname";
	// 	when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
	// 		.thenReturn(new ResponseEntity<>(HttpStatus.OK));
	// 	when(userRepository.existsByBjNickname(bjNickname)).thenReturn(true);
	// 	// when, then
	// 	assertThatThrownBy(() -> userService.checkBjNickname(bjNickname))
	// 		.isInstanceOf(CheckBjNicknameValidationException.class)
	// 		.hasFieldOrPropertyWithValue("code", HttpStatus.CONFLICT.value())
	// 		.hasFieldOrPropertyWithValue("error", "이미 가입된 백준 닉네임 입니다.");
	// }

	@Test
	@DisplayName("백준 닉네임 유효성 검증 실패 : 백준 서버 오류 발생")
	void checkBjNickname_4() {
		// given
		String bjNickname = "bjNickname";
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
			.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		// when, then
		assertThatThrownBy(() -> userService.checkBjNickname(bjNickname))
			.isInstanceOf(BOJServerErrorException.class)
			.hasFieldOrPropertyWithValue("error", "현재 백준 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
	}
}