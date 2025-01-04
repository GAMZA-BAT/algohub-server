package com.gamzabat.algohub.common.logging;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MDCFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		HttpServletRequest nativeRequest = WebUtils.getNativeRequest(request, HttpServletRequest.class);

		MDCUtil.setJsonValue(MDCUtil.MDC_REQUEST_URI, MDCUtil.getRequestUri(nativeRequest));
		MDCUtil.set(MDCUtil.MDC_REQUEST_METHOD, MDCUtil.getMethod(nativeRequest));
		MDCUtil.set(MDCUtil.MDC_HEADER, MDCUtil.getHeader(nativeRequest));
		MDCUtil.set(MDCUtil.MDC_PARAMETER, MDCUtil.getParameter(nativeRequest));
		MDCUtil.set(MDCUtil.MDC_REQUEST_BODY, MDCUtil.getBody(nativeRequest));

		filterChain.doFilter(request, response);

	}
}
