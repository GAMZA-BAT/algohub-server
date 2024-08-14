package com.gamzabat.algohub.feature.user.controller;

import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.dto.*;
import com.gamzabat.algohub.feature.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "회원 컨트롤러", description = "회원 관련된 API 명세서")
public class UserController {
	private final UserService userService;

	@PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "회원 가입 API")
	public ResponseEntity<Object> register(@Valid @RequestPart RegisterRequest request, Errors errors,
										   @RequestPart(required = false) MultipartFile profileImage){
		if(errors.hasErrors())
			throw new RequestException("올바르지 않은 요청입니다.",errors);
		userService.register(request, profileImage);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping(value = "/sign-in")
	@Operation(summary = "로그인 API")
	public ResponseEntity<Object> signIn(@Valid @RequestBody SignInRequest request, Errors errors){
		if(errors.hasErrors())
			throw new RequestException("로그인 요청이 올바르지 않습니다.",errors);
		SignInResponse response = userService.signIn(request);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping()
	@Operation(summary = "회원정보조회 API")
	public ResponseEntity<UserInfoResponse> userInfo(@AuthedUser User user){
		UserInfoResponse userInfo = userService.userInfo(user);
		return ResponseEntity.ok().body(userInfo);
	}

	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "회원정보수정 API")
	public ResponseEntity<Object> updateInfo(@AuthedUser User user, @RequestPart UpdateUserRequest request, @RequestPart(required = false) MultipartFile profileImage){

		userService.userUpdate(user, request,profileImage);

		return ResponseEntity.ok().body("OK");
	}

	@DeleteMapping()
	@Operation(summary = "회원정보삭제 API")
	public ResponseEntity<Object> deleteUser(@AuthedUser User user, @Valid @RequestBody DeleteUserRequest request, Errors errors){
		if (errors.hasErrors()) {
			throw new RequestException("올바르지 않은 요청입니다.",errors);
		}

		userService.deleteUser(user, request);

		return ResponseEntity.ok().body("회원정보를 삭제했습니다.");

	}

	@DeleteMapping("/logout")
	@Operation(summary = "로그아웃 API")
	public ResponseEntity<Object> logout(HttpServletRequest request){
		userService.logout(request);
		return ResponseEntity.ok().body("OK");
	}
}

// 파일이 변경됐어요~!~!
// pr
// issue
