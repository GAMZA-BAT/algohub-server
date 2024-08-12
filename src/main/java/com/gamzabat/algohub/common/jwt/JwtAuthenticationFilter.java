package com.gamzabat.algohub.common.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	private final TokenProvider tokenProvider;
	private final List<String> excludedPaths = Arrays.asList("/api/user/sign-in","/api/user/register");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException{
		String path = request.getRequestURI();
		return excludedPaths.stream().anyMatch(path::startsWith);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		if (shouldNotFilter(request)){
			filterChain.doFilter(request,response);
			return;
		}
		try{
			String token = tokenProvider.resolveToken(request);
			if (token != null && tokenProvider.validateToken(token)) {
				Authentication authentication = tokenProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			filterChain.doFilter(request, response);
		} catch (JwtRequestException e) {
			sendErrorResponse(response,e);
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
