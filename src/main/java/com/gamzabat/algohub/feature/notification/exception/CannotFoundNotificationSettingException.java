package com.gamzabat.algohub.feature.notification.exception;

import lombok.Getter;

@Getter
public class CannotFoundNotificationSettingException extends RuntimeException {
	private final String error;

	public CannotFoundNotificationSettingException(String error) {
		this.error = error;
	}
}
