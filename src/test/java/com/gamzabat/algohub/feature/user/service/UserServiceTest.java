package com.gamzabat.algohub.feature.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import com.gamzabat.algohub.enums.ImageType;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundUserException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.user.domain.ResetPassword;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.dto.DeleteUserRequest;
import com.gamzabat.algohub.feature.user.dto.EditUserPasswordRequest;
import com.gamzabat.algohub.feature.user.dto.RegisterRequest;
import com.gamzabat.algohub.feature.user.dto.ResetPasswordRequest;
import com.gamzabat.algohub.feature.user.dto.SignInRequest;
import com.gamzabat.algohub.feature.user.dto.TokenResponse;
import com.gamzabat.algohub.feature.user.dto.UpdateUserRequest;
import com.gamzabat.algohub.feature.user.dto.UserInfoResponse;
import com.gamzabat.algohub.feature.user.exception.BOJServerErrorException;
import com.gamzabat.algohub.feature.user.exception.CheckBjNicknameValidationException;
import com.gamzabat.algohub.feature.user.exception.CheckNicknameValidationException;
import com.gamzabat.algohub.feature.user.exception.CheckPasswordFormException;
import com.gamzabat.algohub.feature.user.exception.ResetPasswordValidationError;
import com.gamzabat.algohub.feature.user.exception.UncorrectedPasswordException;
import com.gamzabat.algohub.feature.user.repository.ResetPasswordRepository;
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
	@Mock
	private ResetPasswordRepository resetPasswordRepository;
	@Mock
	private EmailService emailService;
	@Captor
	private ArgumentCaptor<User> userCaptor;
	@Captor
	private ArgumentCaptor<ResetPassword> passwordCaptor;

	private final String email = "test@email.com";
	private final String password = "password1!";
	private final String nickname = "nickname";
	private final String encoded = "encoded";
	private final String imageUrl = "1_test@email.com_image.jpg";
	private final String bjNickname = "bjNickname";
	private final String RESET_PASSWORD_TOKEN = "RESET_PASSWORD_TOKEN";
	private User user;
	private ResetPassword resetPassword;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder()
			.email(email)
			.password(encoded)
			.nickname(nickname)
			.bjNickname(bjNickname)
			.profileImage(imageUrl)
			.role(Role.USER)
			.build();

		Field userId = User.class.getDeclaredField("id");
		userId.setAccessible(true);
		userId.set(user, 1L);

		resetPassword = ResetPassword.builder()
			.user(user)
			.token(RESET_PASSWORD_TOKEN)
			.build();
		Field resetId = ResetPassword.class.getDeclaredField("id");
		resetId.setAccessible(true);
		resetId.set(resetPassword, 1L);
	}

	@Test
	@DisplayName("회원가입 성공 : 이미지 포함")
	void register() {
		// given
		String prefix = "1_test@email.com";
		RegisterRequest request = new RegisterRequest(email, password, nickname, bjNickname);
		MockMultipartFile profileImage = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test".getBytes());
		when(userRepository.save(any(User.class))).thenReturn(user);
		when(imageService.createImagePrefix(user.getId(), user.getEmail())).thenReturn(prefix);
		when(imageService.saveImage(ImageType.USER, prefix,
			profileImage)).thenReturn(imageUrl);
		when(passwordEncoder.encode(password)).thenReturn(encoded);
		// when
		userService.register(request, profileImage);
		// then
		verify(userRepository, times(1)).save(userCaptor.capture());
		User user = userCaptor.getValue();
		assertThat(user.getEmail()).isEqualTo(email);
		assertThat(user.getRole()).isEqualTo(Role.USER);
		assertThat(user.getDescription()).isEqualTo("");
		verify(imageService, times(1)).createImagePrefix(anyLong(), anyString());
		verify(imageService, times(1)).saveImage(ImageType.USER, prefix, profileImage);
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
		String accessToken = "access-token";
		String refreshToken = "refresh-token";
		Authentication authentication = mock(Authentication.class);
		JwtDTO jwtDTO = new JwtDTO("Baerer", accessToken, refreshToken);
		AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
		when(authManager.getObject()).thenReturn(authenticationManager);
		when(authManager.getObject().authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
			authentication);
		when(tokenProvider.generateTokens(authentication)).thenReturn(jwtDTO);
		// when
		TokenResponse response = userService.signIn(request);
		// then
		assertThat(response.accessToken()).isEqualTo(accessToken);
		assertThat(response.refreshToken()).isEqualTo(refreshToken);
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
		assertThat(response.getDescription()).isEqualTo("");
	}

	@Test
	@DisplayName("회원 정보 수정 : 프로필 이미지를 null로 설정")
	void userUpdateWithoutImage() {
		// given
		UpdateUserRequest request = new UpdateUserRequest("newNickname", "newBjNickname", "I am Batman", true);
		doNothing().when(imageService).deleteImage(imageUrl);
		// when
		userService.userUpdate(user, request, null);
		// then
		assertThat(user.getNickname()).isEqualTo("newNickname");
		assertThat(user.getBjNickname()).isEqualTo("newBjNickname");
		assertThat(user.getDescription()).isEqualTo("I am Batman");
		assertThat(user.getProfileImage()).isNull();
		verify(imageService, times(1)).deleteImage(imageUrl);
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
		when(tokenProvider.getAccessTokenExpirationTime()).thenReturn(6000L);
		// when
		userService.logout(request);
		// then
		verify(redisService, times(1)).setValues(eq(token), eq("logout"), eq(Duration.ofMillis(6000L)));
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
	@DisplayName("비밀번호 수정 성공")
	void editPasswordSuccess() {
		//given
		EditUserPasswordRequest request = new EditUserPasswordRequest("password", "password1");
		when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(true);
		//when
		userService.editPassword(user, request);
		//then
		verify(userRepository).save(user);
	}

	@Test
	@DisplayName("비밀번호 수정 실패 : 현재의 비밀번호가 틀린경우")
	void editPasswordFailed_1() {
		//given
		EditUserPasswordRequest request = new EditUserPasswordRequest("password22", "password1");
		when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(false);

		//when,then
		assertThatThrownBy(() -> userService.editPassword(user, request))
			.isInstanceOf(UncorrectedPasswordException.class)
			.hasFieldOrPropertyWithValue("errors", "비밀번호가 틀렸습니다.");
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

	@Test
	@DisplayName("닉네임 중복 검사")
	void checkNickname_1() {
		// given
		when(userRepository.existsByNickname(nickname)).thenReturn(false);
		// when
		userService.checkNickname(nickname);
		// then
	}

	@ParameterizedTest
	@ValueSource(strings = {"***asdf", "16asdfasdfnicknameasdf", "ab"})
	@DisplayName("닉네임 중복 검사 : 잘못된 형식의 닉네임")
	void checkNickname_2(String invalidNickname) {
		// given
		// when, then
		assertThatThrownBy(() -> userService.checkNickname(invalidNickname))
			.isInstanceOf(CheckNicknameValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "닉네임은 영문과 숫자로 구성된 3~16글자여야 합니다.");
	}

	@Test
	@DisplayName("닉네임 중복 검사 : 이미 사용 중인 닉네임")
	void checkNickname_3() {
		// given
		when(userRepository.existsByNickname(nickname)).thenReturn(true);
		// when, then
		assertThatThrownBy(() -> userService.checkNickname(nickname))
			.isInstanceOf(CheckNicknameValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.CONFLICT.value())
			.hasFieldOrPropertyWithValue("error", "이미 사용 중인 닉네임입니다.");
	}

	@Test
	@DisplayName("타회원 정보 조회 성공")
	void otherUserInfo_success() {
		// given
		User user2 = User.builder()
			.email("otherUserEmail")
			.nickname("otherUserNickname")
			.bjNickname("otherUserBjNickname")
			.profileImage("otherUserProfileImage")
			.role(Role.USER)
			.build();
		// when
		when(userRepository.findByNickname(user2.getNickname())).thenReturn(Optional.of(user2));
		UserInfoResponse response = userService.otherUserInfo(user2.getNickname());
		// then
		assertThat(response.getEmail()).isEqualTo("otherUserEmail");
		assertThat(response.getNickname()).isEqualTo("otherUserNickname");
		assertThat(response.getProfileImage()).isEqualTo("otherUserProfileImage");
		assertThat(response.getBjNickname()).isEqualTo("otherUserBjNickname");
		assertThat(response.getDescription()).isEqualTo("");
	}

	@Test
	@DisplayName("타회원 정보 조회 실패")
	void otherUserInfo_failed() {
		// given
		when(userRepository.findByNickname("nickname2")).thenReturn(Optional.empty());

		// then
		assertThatThrownBy(() -> userService.otherUserInfo("nickname2"))
			.isInstanceOf(CannotFoundUserException.class)
			.satisfies(exception -> {
				CannotFoundUserException ex = (CannotFoundUserException)exception;
				assertThat(ex.getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
				assertThat(ex.getError()).isEqualTo("해당 유저는 존재하지 않습니다.");
			});
	}

	@Test
	@DisplayName("비밀번호 변경 메일 발송 실패 : 존재하지 않는 유저 ")
	void resetPasswordEmail_failed1() {
		//given
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		//when, then
		assertThatThrownBy(() -> userService.sendResetPasswordMail(email))
			.isInstanceOf(CannotFoundUserException.class)
			.satisfies(exception -> {
				CannotFoundUserException ex = (CannotFoundUserException)exception;
				assertThat(ex.getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
				assertThat(ex.getError()).isEqualTo("존재하지 않는 이메일의 유저입니다.");
			});
	}

	@Test
	@DisplayName("비밀번호 변경 메일 발송 성공")
	void resetPasswordEmail_suceess() {
		//give
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(emailService.sendResetPasswordMail(eq(email), anyString())).thenReturn(
			CompletableFuture.completedFuture(null));

		//when
		userService.sendResetPasswordMail(email);

		//then
		verify(resetPasswordRepository, times(1)).save(passwordCaptor.capture());
		ResetPassword resetPassword = passwordCaptor.getValue();
		assertThat(resetPassword.getUser()).isEqualTo(user);

		verify(emailService, times(1)).sendResetPasswordMail(anyString(), anyString());

	}

	@Test
	@DisplayName("비밀번호 변경 실패 : 없는 Token")
	void resetPassword_failed1() {
		//give
		ResetPasswordRequest request = new ResetPasswordRequest(RESET_PASSWORD_TOKEN, password);
		when(resetPasswordRepository.findByToken(RESET_PASSWORD_TOKEN)).thenReturn(Optional.empty());

		//when, then
		assertThatThrownBy(() -> userService.resetPassword(request))
			.isInstanceOf(ResetPasswordValidationError.class)
			.satisfies(exception -> {
				ResetPasswordValidationError ex = (ResetPasswordValidationError)exception;
				assertThat(ex.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
				assertThat(ex.getErrors()).isEqualTo("유효하지 않은 요청입니다.");
			});
	}

	@Test
	@DisplayName("비밀번호 변경 실패 : 만료된 요청")
	void resetPassword_failed2() {
		//give
		LocalDateTime now = LocalDateTime.now();
		ResetPasswordRequest request = new ResetPasswordRequest(RESET_PASSWORD_TOKEN, password);
		when(resetPasswordRepository.findByToken(RESET_PASSWORD_TOKEN)).thenReturn(Optional.of(resetPassword));

		//when, then
		try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
			mockedStatic.when(LocalDateTime::now).thenReturn(now.plusHours(3));
			assertThatThrownBy(() -> userService.resetPassword(request))
				.isInstanceOf(ResetPasswordValidationError.class)
				.satisfies(exception -> {
					ResetPasswordValidationError ex = (ResetPasswordValidationError)exception;
					assertThat(ex.getCode()).isEqualTo(HttpStatus.GONE.value());
					assertThat(ex.getErrors()).isEqualTo("기한이 만료된 비밀번호 수정 요청입니다.");
				});
		}

	}

	@Test
	@DisplayName("비밀번호 변경 실패 : 이미 완료된 토큰")
	void resetPassword_failed3() {
		//give
		ResetPasswordRequest request = new ResetPasswordRequest(RESET_PASSWORD_TOKEN, password);
		when(resetPasswordRepository.findByToken(RESET_PASSWORD_TOKEN)).thenReturn(Optional.of(resetPassword));
		resetPassword.makeDone();
		//when, then
		assertThatThrownBy(() -> userService.resetPassword(request))
			.isInstanceOf(ResetPasswordValidationError.class)
			.satisfies(exception -> {
				ResetPasswordValidationError ex = (ResetPasswordValidationError)exception;
				assertThat(ex.getCode()).isEqualTo(HttpStatus.CONFLICT.value());
				assertThat(ex.getErrors()).isEqualTo("이미 수정이 완료된 요청입니다.");
			});
	}

	@Test
	@DisplayName("비밀번호 변경 실패 : 비밀번호 조건 만족 안함")
	void resetPassword_failed4() {
		//give
		ResetPasswordRequest request = new ResetPasswordRequest(RESET_PASSWORD_TOKEN, "pass");
		when(resetPasswordRepository.findByToken(RESET_PASSWORD_TOKEN)).thenReturn(Optional.of(resetPassword));
		//when, then
		assertThatThrownBy(() -> userService.resetPassword(request))
			.isInstanceOf(CheckPasswordFormException.class)
			.satisfies(exception -> {
				CheckPasswordFormException ex = (CheckPasswordFormException)exception;
				assertThat(ex.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
				assertThat(ex.getErrors()).isEqualTo("올바르지 않은 비밀번호 형식입니다");
			});
	}

	@Test
	@DisplayName("비밀번호 변경 성공")
	void resetPassword_success() {
		//give
		ResetPasswordRequest request = new ResetPasswordRequest(RESET_PASSWORD_TOKEN, password);
		when(resetPasswordRepository.findByToken(RESET_PASSWORD_TOKEN)).thenReturn(Optional.of(resetPassword));
		//when
		userService.resetPassword(request);
		//then
		verify(passwordEncoder, times(1)).encode(password);
		assertThat(resetPassword.getDone()).isTrue();
		assertThat(user.getPassword()).isEqualTo(passwordEncoder.encode(password));
	}

}
