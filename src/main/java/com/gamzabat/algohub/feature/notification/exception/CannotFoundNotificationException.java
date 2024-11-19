package com.gamzabat.algohub.feature.notification.exception;

import lombok.Getter;

@Getter
public class CannotFoundNotificationException extends RuntimeException {
	private final String error;

	public CannotFoundNotificationException(String error) {
		this.error = error;
	}
}
