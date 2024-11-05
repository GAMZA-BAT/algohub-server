package com.gamzabat.algohub.feature.notice.exception;

import lombok.Getter;

@Getter
public class NoticeValidationException extends RuntimeException {
	private final String error;

	public NoticeValidationException(String error) {
		this.error = error;
	}
}
