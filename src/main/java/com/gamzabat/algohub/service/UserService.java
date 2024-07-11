package com.gamzabat.algohub.service;


import com.gamzabat.algohub.dto.*;
import com.gamzabat.algohub.exception.UncorrectedPasswordException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.domain.User;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final ImageService imageService;
	private final TokenProvider tokenProvider;
	private final AuthenticationManagerBuilder authManager;

	public void register(RegisterRequest request, MultipartFile profileImage) {
		checkEmailDuplication(request.email());
		String imageUrl = imageService.saveImage(profileImage);
		String encodedPassword = passwordEncoder.encode(request.password());
		userRepository.save(User.builder()
			.email(request.email())
			.password(encodedPassword)
			.nickname(request.nickname())
			.profileImage(imageUrl)
			.role(Role.USER)
			.build());
		log.info("success to register");
	}

	public SignInResponse signIn(SignInRequest request) {
		UsernamePasswordAuthenticationToken authenticationToken
			= new UsernamePasswordAuthenticationToken(request.email(),request.password());
		Authentication authenticate = authManager.getObject().authenticate(authenticationToken);

		JwtDTO token = tokenProvider.generateToken(authenticate);
		return new SignInResponse(token.getToken());
	}

	private void checkEmailDuplication(String email){
		if(userRepository.existsByEmail(email))
			throw new UserValidationException("이미 가입 된 이메일 입니다.");
	}


	public UserInfoResponse userInfo(User user) {
		return new UserInfoResponse(user.getEmail(), user.getNickname(),user.getProfileImage());
	}

	public void userUpdate(User user, UpdateUserRequest updateUserRequest, MultipartFile profileImage) {

		if (profileImage != null && !profileImage.isEmpty()) {
			if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
				imageService.deleteImage(user.getProfileImage());
				String imageUrl = imageService.saveImage(profileImage);
				user.editProfileImage(imageUrl);
			}
		}
		if (updateUserRequest.getNickname() != null && !updateUserRequest.getNickname().isEmpty()) {
			user.editNickname(updateUserRequest.getNickname());
		}

		userRepository.save(user);
	}

	public void deleteUser( User user, DeleteUserRequest deleteUserRequest) {

		if (!passwordEncoder.matches(deleteUserRequest.password(),user.getPassword()))
		{
			throw new UncorrectedPasswordException("비밀번호가 틀렸습니다.");
		}
		userRepository.delete(user);
	}
}
