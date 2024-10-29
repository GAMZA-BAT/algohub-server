package com.gamzabat.algohub.feature.notification.enums;

public enum NotificationMessage {
	PROBLEM_STARTED("[%s] 문제가 시작되었습니다! 지금 도전해보세요!");

	private final String message;

	NotificationMessage(String message) {
		this.message = message;
	}

	public String format(String... args) {
		return String.format(message, args);
	}
}
