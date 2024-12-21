package com.gamzabat.algohub.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {
	private final AuthService authService;

	@PostMapping("/oauth/github/sign-in")
	public void signIn(@RequestParam("code") String code) {
		authService.oauthSignIn(code);
	}
}
