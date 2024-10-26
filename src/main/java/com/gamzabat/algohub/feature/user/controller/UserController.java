package com.gamzabat.algohub.feature.user.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.dto.CheckEmailRequest;
import com.gamzabat.algohub.feature.user.dto.DeleteUserRequest;
import com.gamzabat.algohub.feature.user.dto.EditUserPasswordRequest;
import com.gamzabat.algohub.feature.user.dto.RegisterRequest;
import com.gamzabat.algohub.feature.user.dto.SignInRequest;
import com.gamzabat.algohub.feature.user.dto.SignInResponse;
import com.gamzabat.algohub.feature.user.dto.UpdateUserRequest;
import com.gamzabat.algohub.feature.user.dto.UserInfoResponse;
import com.gamzabat.algohub.feature.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "회원 API", description = "회원 관련된 API 명세서")
public class UserController {
	private final UserService userService;

	@PostMapping(value = "/sign-up", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "회원 가입 API")
	public ResponseEntity<Object> register(@Valid @RequestPart RegisterRequest request, Errors errors,
		@RequestPart(required = false) MultipartFile profileImage) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 요청입니다.", errors);
		userService.register(request, profileImage);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping(value = "/sign-in")
	@Operation(summary = "로그인 API")
	public ResponseEntity<Object> signIn(@Valid @RequestBody SignInRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("로그인 요청이 올바르지 않습니다.", errors);
		SignInResponse response = userService.signIn(request);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping()
	@Operation(summary = "회원정보조회 API")
	public ResponseEntity<UserInfoResponse> userInfo(@AuthedUser User user) {
		UserInfoResponse userInfo = userService.userInfo(user);
		return ResponseEntity.ok().body(userInfo);
	}

	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "회원정보수정 API")
	public ResponseEntity<Object> updateInfo(@AuthedUser User user, @RequestPart UpdateUserRequest request,
		@RequestPart(required = false) MultipartFile profileImage) {

		userService.userUpdate(user, request, profileImage);
		return ResponseEntity.ok().body("OK");
	}

	@DeleteMapping()
	@Operation(summary = "회원정보삭제 API")
	public ResponseEntity<Object> deleteUser(@AuthedUser User user, @Valid @RequestBody DeleteUserRequest request,
		Errors errors) {
		if (errors.hasErrors()) {
			throw new RequestException("올바르지 않은 요청입니다.", errors);
		}
		userService.deleteUser(user, request);
		return ResponseEntity.ok().body("회원정보를 삭제했습니다.");
	}

	@DeleteMapping("/logout")
	@Operation(summary = "로그아웃 API")
	public ResponseEntity<Object> logout(HttpServletRequest request) {
		userService.logout(request);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping("/check-baekjoon-nickname")
	@Operation(summary = "백준 닉네임 유효성 검증 API", description = "회원가입 진행 시, 백준 닉네임이 유효한지 검증하는 API")
	public ResponseEntity<String> checkBjNickname(@RequestParam String bjNickname) {
		userService.checkBjNickname(bjNickname);
		return ResponseEntity.ok().body("OK");
	}

	@PatchMapping("/edit-password")
	@Operation(summary = "비밀번호 변경 API")
	public ResponseEntity<Object> editPassword(@AuthedUser User user,
		@Valid @RequestBody EditUserPasswordRequest request, Errors errors) {
		if (errors.hasErrors()) {
			throw new RequestException("올바르지 않은 요청입니다.", errors);
		}
		userService.editPassword(user, request);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/check-email")
	@Operation(summary = "이메일 중복 검사 API", description = "회원가입 진행 시, 이메일 형태 및 중복을 검사하는 API")
	public ResponseEntity<String> checkEmailDuplication(@Valid @RequestBody CheckEmailRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("이메일 중복 검사 요청이 올바르지 않습니다.", errors);

		userService.checkEmailDuplication(request.email());
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping("/check-nickname")
	@Operation(summary = "닉네임 중복 검사 API", description = "회원가입 진행 시, 닉네임 형식 및 중복을 검사하는 API")
	public ResponseEntity<String> checkNickname(@RequestParam String nickname) {
		userService.checkNickname(nickname);
		return ResponseEntity.ok().body("OK");
	}
}
