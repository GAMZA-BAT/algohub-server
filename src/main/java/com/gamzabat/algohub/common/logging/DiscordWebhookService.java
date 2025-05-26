package com.gamzabat.algohub.common.logging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookService {
	@Value("${noti_url:}")
	private String webhookUrl;

	private final RestTemplate restTemplate;

	public void sendRegisterMessage(String username, String type, Long sid) {
		String content = username + " 님이 " + type + " 방식으로 " + sid + "번째로 회원가입 하셨습니다!! 🎉";
		this.send(content);
	}

	public void sendCreateGroupMessage(String username, String groupName) {
		String content = username + " 님이 " + groupName + " 그룹을 생성하셨습니다 😎";
		this.send(content);
	}

	private void send(String message) {
		try {
			if (!StringUtils.hasText(webhookUrl)) {
				log.debug("No webhook url provided");
				return;
			}
			Map<String, String> payload = new HashMap<>();
			payload.put("content", message);
			restTemplate.postForObject(webhookUrl, payload, String.class);
			log.info("Webhook sent: {}", message);
		} catch (Exception e) {
			log.error("Error sending webhook message", e);
		}
	}

}
