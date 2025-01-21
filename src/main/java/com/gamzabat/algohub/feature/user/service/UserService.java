package com.gamzabat.algohub.feature.user.service;

import static com.gamzabat.algohub.constants.ApiConstants.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;

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
import com.gamzabat.algohub.common.jwt.dto.ReissueTokenRequest;
import com.gamzabat.algohub.common.redis.RedisService;
import com.gamzabat.algohub.enums.ImageType;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundUserException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.dto.DeleteUserRequest;
import com.gamzabat.algohub.feature.user.dto.EditUserPasswordRequest;
import com.gamzabat.algohub.feature.user.dto.RegisterRequest;
import com.gamzabat.algohub.feature.user.dto.SignInRequest;
import com.gamzabat.algohub.feature.user.dto.TokenResponse;
import com.gamzabat.algohub.feature.user.dto.UpdateUserRequest;
import com.gamzabat.algohub.feature.user.dto.UserInfoResponse;
import com.gamzabat.algohub.feature.user.exception.BOJServerErrorException;
import com.gamzabat.algohub.feature.user.exception.CheckBjNicknameValidationException;
import com.gamzabat.algohub.feature.user.exception.CheckEmailFormException;
import com.gamzabat.algohub.feature.user.exception.CheckNicknameValidationException;
import com.gamzabat.algohub.feature.user.exception.CheckPasswordFormException;
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
		checkNickname(request.nickname());
		checkEmailForm(request.email());
		checkBjNickname(request.bjNickname());
		checkPasswordForm(request.password());

		String encodedPassword = passwordEncoder.encode(request.password());

		User user = userRepository.save(User.builder()
			.email(request.email())
			.password(encodedPassword)
			.nickname(request.nickname())
			.bjNickname(request.bjNickname())
			.role(Role.USER)
			.build());

		saveProfileImage(profileImage, user);
		log.info("success to register");
	}

	private void saveProfileImage(MultipartFile profileImage, User user) {
		String imagePrefix = imageService.createImagePrefix(user.getId(), user.getEmail());
		String imageUrl = imageService.saveImage(ImageType.USER, imagePrefix, profileImage);
		user.editProfileImage(imageUrl);
	}

	@Transactional
	public TokenResponse signIn(SignInRequest request) {
		UsernamePasswordAuthenticationToken authenticationToken
			= new UsernamePasswordAuthenticationToken(request.email(), request.password());
		Authentication authenticate;
		try {
			authenticate = authManager.getObject().authenticate(authenticationToken);
		} catch (BadCredentialsException e) {
			throw new UncorrectedPasswordException("비밀번호가 틀렸습니다.");
		}
		JwtDTO result = tokenProvider.generateTokens(authenticate);
		log.info("success to sign in");
		return new TokenResponse(result.getAccessToken(), result.getRefreshToken());
	}

	@Transactional(readOnly = true)
	public UserInfoResponse userInfo(User user) {
		return new UserInfoResponse(user.getEmail(), user.getNickname(), user.getProfileImage(), user.getBjNickname(),
			user.getDescription());
	}

	@Transactional
	public void userUpdate(User user, UpdateUserRequest updateUserRequest, MultipartFile profileImage) {
		editUserProfileImage(user, profileImage, updateUserRequest.getIsDefaultImage());
		checkNickname(updateUserRequest.getNickname());

		if (!updateUserRequest.getNickname().isEmpty()) {
			user.editNickname(updateUserRequest.getNickname());
		}
		if (updateUserRequest.getBjNickname() != null && !updateUserRequest.getBjNickname().isEmpty()) {
			user.editBjNickname(updateUserRequest.getBjNickname());
		}
		if (updateUserRequest.getDescription() != null && !updateUserRequest.getDescription().isEmpty()) {
			user.editDescription(updateUserRequest.getDescription());
		}

		userRepository.save(user);
		log.info("success to update user");
	}

	private void editUserProfileImage(User user, MultipartFile inputImage, Boolean isDefaultImage) {
		if (inputImage != null) {
			if (user.getProfileImage() != null) {
				imageService.deleteImage(user.getProfileImage());
			}
			saveProfileImage(inputImage, user);
			log.info("success to update user profile image. profile image : {}", user.getProfileImage());
			return;
		}
		if (isDefaultImage) {
			handleNullInputImage(user);
		}

	}

	private void handleNullInputImage(User user) {
		if (user.getProfileImage() != null) {
			imageService.deleteImage(user.getProfileImage());
			user.editProfileImage(null);
		}
	}

	private boolean isEqualToProfileImage(User user, MultipartFile profileImage) {
		String prefix = imageService.createImagePrefix(user.getId(), user.getEmail());
		String inputImageUrl = imageService.getImageName(ImageType.USER, prefix,
			profileImage);
		String userProfileImageUrl = URLDecoder.decode(imageService.parseImageName(user.getProfileImage()),
			StandardCharsets.UTF_8);
		return inputImageUrl.equals(userProfileImageUrl);
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
		long tokenExpiration = tokenProvider.getAccessTokenExpirationTime();
		redisService.setValues(accessToken, "logout", Duration.ofMillis(tokenExpiration));
		log.info("success to logout");
	}

	@Transactional
	public void editPassword(User user, EditUserPasswordRequest request) {
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw new UncorrectedPasswordException("비밀번호가 틀렸습니다.");
		}

		String encodedPassword = passwordEncoder.encode(request.newPassword());
		user.editPassword(encodedPassword);

		userRepository.save(user);
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

	@Transactional(readOnly = true)
	public void checkEmailDuplication(String email) {
		if (userRepository.existsByEmail(email))
			throw new UserValidationException("이미 사용 중인 이메일 입니다.");
	}

	@Transactional(readOnly = true)
	public void checkNickname(String nickname) {
		if (isInvalidNicknameForm(nickname))
			throw new CheckNicknameValidationException(HttpStatus.BAD_REQUEST.value(),
				"닉네임은 영문과 숫자로 구성된 3~16글자여야 합니다.");

		if (userRepository.existsByNickname(nickname))
			throw new CheckNicknameValidationException(HttpStatus.CONFLICT.value(), "이미 사용 중인 닉네임입니다.");

		log.info("success to check nickname validity");
	}

	@Transactional(readOnly = true)
	public UserInfoResponse otherUserInfo(String userNickname) {
		User targetUser = userRepository.findByNickname(userNickname)
			.orElseThrow(() -> new CannotFoundUserException(HttpStatus.NOT_FOUND.value(), "해당 유저는 존재하지 않습니다."));

		return new UserInfoResponse(targetUser.getEmail(), targetUser.getNickname(), targetUser.getProfileImage(),
			targetUser.getBjNickname(),
			targetUser.getDescription());
	}

	private boolean isInvalidNicknameForm(String nickname) {
		String regex = "[^a-zA-Z0-9]";
		return nickname.length() < 3 || nickname.length() > 16 || Pattern.compile(regex).matcher(nickname).find();
	}

	@Transactional
	public TokenResponse reissueToken(ReissueTokenRequest request) {
		String expiredToken = request.expiredAccessToken();
		String refreshToken = request.refreshToken();

		TokenResponse response = tokenProvider.reissueTokens(expiredToken, refreshToken);
		log.info("success to reissue tokens");
		return response;
	}

	private void checkEmailForm(String email) {
		if (!isValidEmailForm(email))
			throw new CheckEmailFormException(HttpStatus.BAD_REQUEST.value(), "이메일 형식이 아닙니다");
	}

	private boolean isValidEmailForm(String email) {

		String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; // 문자 사이에 @를 포함하고 최상위 도메인은 2글자 이상이어야 함
		Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

		if (email == null || email.isEmpty()) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email).matches();
	}

	private void checkPasswordForm(String password) {
		if (!isValidPasswordForm(password))
			throw new CheckPasswordFormException(HttpStatus.BAD_REQUEST.value(), "올바르지 않은 비밀번호 형식입니다");
	}

	private boolean isValidPasswordForm(String password) {

		String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*])[A-Za-z\\d~!@#$%^&*]{8,15}$"; // 영문,숫자,특수문자 만으로 이루어져야 하며 모두 포함하여야 하고 8~15글자 사이여야 함

		if (password == null || password.isEmpty()) {
			return false;
		}
		return password.matches(passwordPattern);

	}

}
