package com.gamzabat.algohub.feature.notification.exception;

import lombok.Getter;

@Getter
public class NotificationValidationException extends RuntimeException {
	private final int code;
	private final String error;

	public NotificationValidationException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
