package com.gamzabat.algohub.common.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Setter
public class DiscordLogAppender extends AppenderBase<ILoggingEvent> {
	private String discordWebhookUrl;

	@Override
	protected void append(ILoggingEvent event) {
		Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

		String levelStr = event.getLevel().levelStr;
		String exceptionBrief = "";
		IThrowableProxy throwable = event.getThrowableProxy();

		if (throwable != null) {
			exceptionBrief = throwable.getClassName() + ": " + throwable.getMessage();
		}

		if (exceptionBrief.isEmpty()) {
			exceptionBrief = "EXCEPTION 정보가 남지 않았습니다.";
		}

		Map<String, Object> embed = new HashMap<>();
		embed.put("title", "[" + levelStr + "] Exception 발생");
		embed.put("color", getColorForLevel(levelStr));
		embed.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

		// Embed Fields
		List<Map<String, String>> fields = new ArrayList<>();
		fields.add(createField("문제 간략 내용", exceptionBrief, false));
		fields.add(createField("요청 URI", escape(mdcPropertyMap.get(MDCUtil.MDC_REQUEST_URI)), true));
		fields.add(createField("HTTP 헤더", escape(mdcPropertyMap.get(MDCUtil.MDC_HEADER)), true));
		fields.add(createField("HTTP 메서드", escape(mdcPropertyMap.get(MDCUtil.MDC_REQUEST_METHOD)), true));
		fields.add(createField("파라미터", escape(mdcPropertyMap.get(MDCUtil.MDC_PARAMETER)), true));
		fields.add(createField("요청 Body", escape(mdcPropertyMap.get(MDCUtil.MDC_REQUEST_BODY)), true));

		embed.put("fields", fields);

		if (throwable != null) {
			String exceptionDetail = ThrowableProxyUtil.asString(throwable);
			fields.add(createField("Exception 상세 내용", truncate(exceptionDetail), false));
		}

		sendToDiscordEmbed(embed);
	}

	private Map<String, String> createField(String name, String value, boolean inline) {
		Map<String, String> field = new HashMap<>();
		field.put("name", name);
		field.put("value", value != null ? value : "없음");
		field.put("inline", String.valueOf(inline));
		return field;
	}

	private void sendToDiscordEmbed(Map<String, Object> embed) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> payload = new HashMap<>();
		payload.put("embeds", List.of(embed));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(discordWebhookUrl, request, String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				addError("Failed to send log message to Discord: " + response.getStatusCode());
			}
		} catch (Exception e) {
			log.warn("Exception occurred while sending log message to Discord", e);
		}
	}

	private String escape(String value) {
		return value != null ? StringEscapeUtils.escapeJson(value) : "없음";
	}

	private String truncate(String value) {
		final int MAX_LENGTH = 1000;
		return value != null && value.length() > MAX_LENGTH ? value.substring(0, MAX_LENGTH) + "..." : value;
	}

	private int getColorForLevel(String level) {
		return switch (level) {
			case "ERROR" -> 0xFF0000; // Red
			case "WARN" -> 0xFFFF00; // Yellow
			case "INFO" -> 0x00FF00; // Green
			case "DEBUG" -> 0x0000FF; // Blue
			case "TRACE" -> 0xAAAAAA; // Gray
			default -> 0x000000; // Black
		};
	}

}
