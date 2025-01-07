package com.gamzabat.algohub.common.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.exception.ErrorResponse;
import com.gamzabat.algohub.exception.JwtRequestException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final TokenProvider tokenProvider;
	private final List<String> excludedPaths = Arrays.asList(
		"/swagger-ui",
		"/v3/api-docs",
		"/api/auth/sign-in",
		"/api/auth/sign-up",
		"/api/auth/reissue-token",
		"/api/users/check-email",
		"/api/users/check-nickname",
		"/api/users/check-baekjoon-nickname");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		if (excludedPaths.stream().anyMatch(path::startsWith))
			return true;

		return isOtherUserInfoEndpoint(path);
	}

	private static boolean isOtherUserInfoEndpoint(String path) {
		Pattern infoPattern = Pattern.compile("^/api/users/(?!me$)[^/]+$");
		Pattern groupsPattern = Pattern.compile("^/api/users/(?!me)[^/]+/groups$");
		return infoPattern.matcher(path).matches() || groupsPattern.matcher(path).matches();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			String token = tokenProvider.resolveToken(request);
			if (token != null && tokenProvider.validateToken(token)) {
				Authentication authentication = tokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			filterChain.doFilter(request, response);
		} catch (JwtRequestException e) {
			sendErrorResponse(response, e);
		}
	}

	private void sendErrorResponse(HttpServletResponse response, JwtRequestException e) throws IOException {
		response.reset();
		response.setStatus(e.getCode());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(new ObjectMapper().writeValueAsString(
			new ErrorResponse(e.getCode(), e.getError(), e.getMessages())
		));
	}
}
