package com.gamzabat.algohub.feature.user.service;

import static com.gamzabat.algohub.constants.ApiConstants.*;

import java.time.Duration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ImageService imageService;
	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authManager;
	private final RedisService redisService;
	private final RestTemplate restTemplate;

	@Transactional
	public void register(RegisterRequest request, MultipartFile profileImage) {
		checkEmailDuplication(request.email());
		String imageUrl = imageService.saveImage(profileImage);
		String encodedPassword = passwordEncoder.encode(request.password());
		userRepository.save(User.builder()
			.email(request.email())
			.password(encodedPassword)
			.nickname(request.nickname())
			.bjNickname(request.bjNickname())
			.profileImage(imageUrl)
			.role(Role.USER)
			.build());
		log.info("success to register");
	}

	@Transactional(readOnly = true)
	public SignInResponse signIn(SignInRequest request) {
		UsernamePasswordAuthenticationToken authenticationToken
			= new UsernamePasswordAuthenticationToken(request.email(), request.password());
		Authentication authenticate;
		try {
			authenticate = authManager.getObject().authenticate(authenticationToken);
		} catch (BadCredentialsException e) {
			throw new UncorrectedPasswordException("비밀번호가 틀렸습니다.");
		}
		JwtDTO token = tokenProvider.generateToken(authenticate);
		return new SignInResponse(token.getToken());
	}

	private void checkEmailDuplication(String email) {
		if (userRepository.existsByEmail(email))
			throw new UserValidationException("이미 가입 된 이메일 입니다.");
	}

	@Transactional(readOnly = true)
	public UserInfoResponse userInfo(User user) {
		return new UserInfoResponse(user.getEmail(), user.getNickname(), user.getProfileImage(), user.getBjNickname());
	}

	@Transactional
	public void userUpdate(User user, UpdateUserRequest updateUserRequest, MultipartFile profileImage) {

		if (profileImage != null && !profileImage.isEmpty()) {
			if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
				imageService.deleteImage(user.getProfileImage());
			}
			String imageUrl = imageService.saveImage(profileImage);
			user.editProfileImage(imageUrl);
		}
		if (updateUserRequest.getNickname() != null && !updateUserRequest.getNickname().isEmpty()) {
			user.editNickname(updateUserRequest.getNickname());
		}
		if (updateUserRequest.getBjNickname() != null && !updateUserRequest.getBjNickname().isEmpty()) {
			user.editBjNickname(updateUserRequest.getBjNickname());
		}

		userRepository.save(user);
	}

	@Transactional
	public void deleteUser(User user, DeleteUserRequest deleteUserRequest) {

		if (!passwordEncoder.matches(deleteUserRequest.password(), user.getPassword())) {
			throw new UncorrectedPasswordException("비밀번호가 틀렸습니다.");
		}
		userRepository.delete(user);
	}

	@Transactional
	public void logout(HttpServletRequest request) {
		String accessToken = tokenProvider.resolveToken(request);
		if (accessToken == null)
			throw new JwtRequestException(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", "토큰이 비어있습니다.");

		long tokenExpiration = tokenProvider.getTokenExpiration();
		redisService.setValues(accessToken, "logout", Duration.ofMillis(tokenExpiration));
		log.info("success to logout");
	}

	@Transactional(readOnly = true)
	public void checkBjNickname(String bjNickname) {
		String bjUserUrl = BOJ_USER_PROFILE_URL + bjNickname;

		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent",
			"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			restTemplate.exchange(bjUserUrl, HttpMethod.GET, entity, String.class);
			// TODO : 백준 본인 인증 관련 사항 확정 후 로직 수정
			// if (userRepository.existsByBjNickname(bjNickname))
			// 	throw new CheckBjNicknameValidationException(HttpStatus.CONFLICT.value(), "이미 가입된 백준 닉네임 입니다.");
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND)
				throw new CheckBjNicknameValidationException(HttpStatus.NOT_FOUND.value(), "백준 닉네임이 유효하지 않습니다.");
		} catch (HttpServerErrorException e) {
			log.error("BOJ server error occurred : " + e.getMessage());
			throw new BOJServerErrorException("현재 백준 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
		log.info("success to check baekjoon nickname validity");
	}
}
