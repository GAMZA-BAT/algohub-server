package com.gamzabat.algohub.auth.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.gamzabat.algohub.auth.dto.EmailDto;
import com.gamzabat.algohub.auth.dto.OAuthInfo;
import com.gamzabat.algohub.constants.ApiConstants;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	@Value("${github.client_id}")
	private String clientId;
	@Value("${github.client_secret}")
	private String secretKey;
	private final RestTemplate restTemplate;
	private final UserRepository userRepository;

	private final String CLIENT_ID_PARAM = "client_id";
	private final String CLIENT_SECRET_PARAM = "client_secret";
	private final String CODE_PARAM = "code";

	@Transactional
	public void oauthSignIn(String code) {
		ResponseEntity<OAuthInfo> response = restTemplate.exchange(
			ApiConstants.GITHUB_ACCESS_TOKEN_URL,
			HttpMethod.POST,
			getAccessToken(code),
			OAuthInfo.class);

		String accessToken = response.getBody().getAccessToken();

		githubLoginSuccess(accessToken);
	}

	private void githubLoginSuccess(String accessToken) {
		ResponseEntity<List<EmailDto>> emails = restTemplate.exchange(
			ApiConstants.GITHUB_EMAIL_URL,
			HttpMethod.GET,
			getUserInfo(accessToken),
			new ParameterizedTypeReference<>() {
			});

		String email = emails.getBody().getFirst().getEmail();
		userRepository.findByEmail(email);
	}

	private HttpEntity<MultiValueMap<String, String>> getAccessToken(String code) {
		LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(CLIENT_ID_PARAM, clientId);
		params.add(CLIENT_SECRET_PARAM, secretKey);
		params.add(CODE_PARAM, code);

		HttpHeaders headers = new HttpHeaders(); // 얘 꼭 필요한가?
		return new HttpEntity<>(params, headers);
	}

	private HttpEntity<MultiValueMap<String, String>> getUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "token " + accessToken);
		return new HttpEntity<>(headers);
	}
}
