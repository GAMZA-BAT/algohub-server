package com.gamzabat.algohub.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.auth.service.OAuth2Service;
import com.gamzabat.algohub.feature.user.dto.TokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "oauth 소셜 로그인 관련 API", description = "code를 사용해 Github 소셜 로그인하는 API")
public class OAuth2Controller {
	private final OAuth2Service oAuth2Service;

	@PostMapping("/oauth/github/sign-in")
	@Operation(summary = "Github 소셜 로그인 API", description = "Github 소셜 로그인 후 발급된 code를 전달해 로그인하는 API")
	public ResponseEntity<TokenResponse> signIn(@RequestParam String code) {
		TokenResponse response = oAuth2Service.githubSignIn(code);
		return ResponseEntity.ok(response);
	}
}
