package com.gamzabat.algohub.auth.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.gamzabat.algohub.auth.dto.GithubEmailDto;
import com.gamzabat.algohub.auth.dto.GithubUserDto;
import com.gamzabat.algohub.auth.dto.OAuthAccessTokenDto;
import com.gamzabat.algohub.auth.exception.GithubApiException;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.common.jwt.dto.JwtDTO;
import com.gamzabat.algohub.common.logging.DiscordWebhookService;
import com.gamzabat.algohub.constants.ApiConstants;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.dto.TokenResponse;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {
	private final TokenProvider tokenProvider;
	@Value("${github.client_id}")
	private String clientId;
	@Value("${github.client_secret}")
	private String secretKey;
	private final RestTemplate restTemplate;
	private final UserRepository userRepository;
	private final DiscordWebhookService webhookService;

	@Transactional
	public TokenResponse githubSignIn(String code) {
		String accessToken = getGithubAccessToken(code);
		GithubUserDto githubUser = getGithubUserInfo(accessToken);
		syncWithUser(githubUser);
		Authentication authentication = createGithubAuthentication(githubUser);
		JwtDTO tokens = tokenProvider.generateTokens(authentication);
		return new TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken());
	}

	private void syncWithUser(GithubUserDto githubUser) {
		Optional<User> optionalUser = userRepository.findByEmail(githubUser.getEmail());
		if (optionalUser.isEmpty()) {
			register(githubUser);
			return;
		}
		User user = optionalUser.get();
		user.editGithubName(githubUser.getLogin());
	}

	private void register(GithubUserDto githubUser) {
		User newUser = userRepository.save(User.builder()
			.email(githubUser.getEmail())
			.password(null)
			.nickname(null)
			.bjNickname(null)
			.role(Role.USER)
			.build());
		newUser.editNickname(createTemporaryNickname(githubUser.getLogin(), newUser.getId()));
		newUser.editGithubName(githubUser.getLogin());

		webhookService.sendRegisterMessage(newUser.getNickname(), "Github", newUser.getId());
	}

	private String createTemporaryNickname(String login, Long id) {
		return login + id;
	}

	private String getGithubAccessToken(String code) {
		HttpEntity<OAuthAccessTokenDto> response = restTemplate.exchange(
			ApiConstants.GITHUB_ACCESS_TOKEN_URL,
			HttpMethod.POST,
			createAccessTokenRequest(code),
			OAuthAccessTokenDto.class);

		if (response.getBody() == null) {
			log.error("failed to request GitHub access token : GitHub API response body is null.");
			throw new GithubApiException("Github access token 응답에 실패했습니다.");
		}

		return response.getBody().getAccessToken();
	}

	private GithubUserDto getGithubUserInfo(String accessToken) {
		ResponseEntity<GithubUserDto> response = restTemplate.exchange(
			ApiConstants.GITHUB_USER_URL,
			HttpMethod.GET,
			createAuthorizationHeader(accessToken),
			GithubUserDto.class);

		GithubUserDto user = response.getBody();
		if (user == null) {
			log.error("failed to request GitHub user info : GitHub API response body is null.");
			throw new GithubApiException("Github 사용자 정보를 가져오는데 실패했습니다.");
		}

		if (user.getEmail() == null || user.getEmail().isEmpty()) {
			user.setEmail(getEmail(accessToken));
		}

		return user;
	}

	private String getEmail(String accessToken) {
		ResponseEntity<List<GithubEmailDto>> emails = restTemplate.exchange(
			ApiConstants.GITHUB_EMAIL_URL,
			HttpMethod.GET,
			createAuthorizationHeader(accessToken),
			new ParameterizedTypeReference<>() {
			}
		);

		if (emails.getBody() == null) {
			log.error("failed to request GitHub user email : GitHub API response body is null.");
			throw new GithubApiException("Github 유저의 이메일을 가져오는데 실패했습니다.");
		}

		return emails.getBody().getFirst().getEmail();
	}

	private Authentication createGithubAuthentication(GithubUserDto githubUser) {
		OAuth2User oAuth2User = new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority(Role.USER.toString())),
			Map.of("email", githubUser.getEmail()),
			"email"
		);

		return new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "github");
	}

	private HttpEntity<MultiValueMap<String, String>> createAccessTokenRequest(String code) {
		LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("client_secret", secretKey);
		params.add("code", code);

		return new HttpEntity<>(params, new HttpHeaders());
	}

	private HttpEntity<Map<String, String>> createAuthorizationHeader(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		return new HttpEntity<>(headers);
	}
}
