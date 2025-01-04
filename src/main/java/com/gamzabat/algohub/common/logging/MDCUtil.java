package com.gamzabat.algohub.common.logging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class MDCUtil {
	public static final String MDC_REQUEST_URI = "requestUri";
	public static final String MDC_REQUEST_METHOD = "requestMethod";
	public static final String MDC_REQUEST_BODY = "requestBody";
	public static final String MDC_HEADER = "header";
	public static final String MDC_PARAMETER = "parameter";

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final MDCAdapter mdcAdapter = MDC.getMDCAdapter();

	private MDCUtil() {
	}

	public static void set(String key, Object value) {
		if (value != null) {
			mdcAdapter.put(key, value.toString());
		}
	}

	public static void setJsonValue(String key, Object value) {
		try {
			String json = value != null
				? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)
				: "내용이 없습니다.";
			mdcAdapter.put(key, json);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Failed to serialize value to JSON for key: " + key, e);
		}
	}

	public static String getRequestUri(HttpServletRequest request) {
		return request.getRequestURI();
	}

	public static Map<String, String> getHeader(HttpServletRequest request) {
		Map<String, String> headerMap = new HashMap<>();
		request.getHeaderNames().asIterator()
			.forEachRemaining(name -> {
				if (!name.equals("user-agent")) {
					headerMap.put(name, request.getHeader(name));
				}
			});
		return headerMap;
	}

	public static Map<String, String> getParameter(HttpServletRequest request) {
		Map<String, String> paramMap = new HashMap<>();
		request.getParameterNames().asIterator()
			.forEachRemaining(name -> paramMap.put(name, request.getParameter(name)));

		return paramMap;
	}

	public static String getBody(HttpServletRequest request) {
		RequestBodyWrapper requestBodyWrapper = WebUtils.getNativeRequest(request, RequestBodyWrapper.class);

		if (requestBodyWrapper != null) {
			return requestBodyWrapper.getBody();
		}

		return "requestBody 정보 없음";
	}

	public static String getMethod(HttpServletRequest request) {
		return request.getMethod();
	}
}
