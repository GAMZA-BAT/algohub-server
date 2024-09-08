package com.gamzabat.algohub.feature.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.common.jwt.dto.JwtDTO;
import com.gamzabat.algohub.common.redis.RedisService;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.JwtRequestException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.dto.DeleteUserRequest;
import com.gamzabat.algohub.feature.user.dto.RegisterRequest;
import com.gamzabat.algohub.feature.user.dto.SignInRequest;
import com.gamzabat.algohub.feature.user.dto.SignInResponse;
import com.gamzabat.algohub.feature.user.dto.UpdateUserRequest;
import com.gamzabat.algohub.feature.user.dto.UserInfoResponse;
import com.gamzabat.algohub.feature.user.exception.BOJServerErrorException;
import com.gamzabat.algohub.feature.user.exception.CheckBjNicknameValidationException;
import com.gamzabat.algohub.feature.user.exception.UncorrectedPasswordException;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@InjectMocks
	private UserService userService;
	@Mock
	private ImageService imageService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private TokenProvider tokenProvider;
	@Mock
	private AuthenticationManagerBuilder authManager;
	@Mock
	private RedisService redisService;
	@Mock
	private RestTemplate restTemplate;
	@Captor
	private ArgumentCaptor<User> userCaptor;

	private final String email = "test@email.com";
	private final String password = "password";
	private final String nickname = "nickname";
	private final String encoded = "encoded";
	private final String imageUrl = "imageUrl";
	private final String bjNickname = "bjNickname";

	private User user;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.email(email)
			.password(encoded)
			.nickname(nickname)
			.bjNickname(bjNickname)
			.profileImage(imageUrl)
			.role(Role.USER)
			.build();
	}

	@Test
	@DisplayName("회원가입 성공")
	void register() {
		// given
		RegisterRequest request = new RegisterRequest(email, password, nickname, bjNickname);
		MockMultipartFile profileImage = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test".getBytes());
		when(imageService.saveImage(profileImage)).thenReturn(imageUrl);
		when(passwordEncoder.encode(password)).thenReturn(encoded);
		// when
		userService.register(request, profileImage);
		// then
		verify(userRepository, times(1)).save(userCaptor.capture());
		User user = userCaptor.getValue();
		assertThat(user.getEmail()).isEqualTo(email);
		assertThat(user.getNickname()).isEqualTo(nickname);
		assertThat(user.getProfileImage()).isEqualTo(imageUrl);
		assertThat(user.getRole()).isEqualTo(Role.USER);
		assertThat(user.getBjNickname()).isEqualTo(bjNickname);
	}

	@Test
	@DisplayName("회원가입 실패 : 이미 가입 된 이메일")
	void registerFailed_1() {
		// given
		RegisterRequest request = new RegisterRequest(email, password, nickname, bjNickname);
		MockMultipartFile profileImage = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test".getBytes());
		when(userRepository.existsByEmail(email)).thenReturn(true);
		// when, then
		assertThatThrownBy(() -> userService.register(request, profileImage))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "이미 사용 중인 이메일 입니다.");
	}

	@Test
	@DisplayName("로그인 성공")
	void signIn() {
		// given
		SignInRequest request = new SignInRequest(email, password);
		Authentication authentication = mock(Authentication.class);
		JwtDTO jwtDTO = new JwtDTO("access-token", "mocked-token-string");
		AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
		when(authManager.getObject()).thenReturn(authenticationManager);
		when(authManager.getObject().authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
			authentication);
		when(tokenProvider.generateToken(authentication)).thenReturn(jwtDTO);
		// when
		SignInResponse response = userService.signIn(request);
		// then
		assertThat(response.token()).isEqualTo("mocked-token-string");
	}

	@Test
	@DisplayName("로그인 실패 : 존재하지 않는 회원")
	void signInFailed_1() {
		// given
		SignInRequest request = new SignInRequest("email2", password);
		AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

		when(authManager.getObject()).thenReturn(authenticationManager);
		when(authManager.getObject().authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.thenThrow(new UserValidationException("존재하지 않는 회원입니다."));
		// when, then
		assertThatThrownBy(() -> userService.signIn(request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "존재하지 않는 회원입니다.");
	}

	@Test
	@DisplayName("로그인 실패 : 틀린 비밀번호")
	void signInFailed_2() {
		// given
		SignInRequest request = new SignInRequest(email, password);
		AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
		when(authManager.getObject()).thenReturn(authenticationManager);
		when(authManager.getObject().authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.thenThrow(new UncorrectedPasswordException("비밀번호가 틀렸습니다."));

		// when, then
		assertThatThrownBy(() -> userService.signIn(request))
			.isInstanceOf(UncorrectedPasswordException.class)
			.hasFieldOrPropertyWithValue("errors", "비밀번호가 틀렸습니다.");
	}

	@Test
	@DisplayName("회원 정보 조회")
	void userInfo() {
		// given
		// when
		UserInfoResponse response = userService.userInfo(user);
		// then
		assertThat(response.getEmail()).isEqualTo(email);
		assertThat(response.getNickname()).isEqualTo(nickname);
		assertThat(response.getProfileImage()).isEqualTo(imageUrl);
		assertThat(response.getBjNickname()).isEqualTo(bjNickname);
	}

	@Test
	@DisplayName("회원 정보 수정 성공")
	void userUpdate() {
		// given
		UpdateUserRequest request = new UpdateUserRequest("newNickname", "newBjNickname");
		MockMultipartFile newProfileImage = new MockMultipartFile("newImage", "image.jpg", "image/jpeg",
			"test".getBytes());
		when(imageService.saveImage(newProfileImage)).thenReturn("newProfileImageUrl");
		doNothing().when(imageService).deleteImage(imageUrl);
		// when
		userService.userUpdate(user, request, newProfileImage);
		// then
		assertThat(user.getNickname()).isEqualTo("newNickname");
		assertThat(user.getBjNickname()).isEqualTo("newBjNickname");
		assertThat(user.getProfileImage()).isEqualTo("newProfileImageUrl");
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void deleteUser() {
		// given
		DeleteUserRequest request = new DeleteUserRequest(password);
		when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
		// when
		userService.deleteUser(user, request);
		// then
		verify(userRepository, times(1)).delete(user);
	}

	@Test
	@DisplayName("회원 탈퇴 실패 : 틀린 비밀번호")
	void deleteUserFailed() {
		// given
		DeleteUserRequest request = new DeleteUserRequest(password);
		when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> userService.deleteUser(user, request))
			.isInstanceOf(UncorrectedPasswordException.class)
			.hasFieldOrPropertyWithValue("errors", "비밀번호가 틀렸습니다.");
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logout() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		String token = "mocked-token-string";
		when(tokenProvider.resolveToken(request)).thenReturn(token);
		when(tokenProvider.getTokenExpiration()).thenReturn(6000L);
		// when
		userService.logout(request);
		// then
		verify(redisService, times(1)).setValues(eq(token), eq("logout"), eq(Duration.ofMillis(6000L)));
	}

	@Test
	@DisplayName("로그아웃 실패 : 비어있는 토큰")
	void logoutFailed() {
		// given
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(tokenProvider.resolveToken(request)).thenReturn(null);
		// when, then
		assertThatThrownBy(() -> userService.logout(request))
			.isInstanceOf(JwtRequestException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "BAD_REQUEST")
			.hasFieldOrPropertyWithValue("messages", new ArrayList<>(List.of("토큰이 비어있습니다.")));
	}

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

	@Test
	@DisplayName("이메일 유효성 검증")
	void checkEmail() {
		// given
		when(userRepository.existsByEmail(email)).thenReturn(false);
		// when
		userService.checkEmailDuplication(email);
		// then
		verify(userRepository, times(1)).existsByEmail(email);
	}

	@Test
	@DisplayName("이메일 유효성 검증 실패 : 이미 가입된 이메일")
	void checkEmailFailed() {
		// given
		when(userRepository.existsByEmail(email)).thenReturn(true);
		// when, then
		assertThatThrownBy(() -> userService.checkEmailDuplication(email))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "이미 사용 중인 이메일 입니다.");
	}
}