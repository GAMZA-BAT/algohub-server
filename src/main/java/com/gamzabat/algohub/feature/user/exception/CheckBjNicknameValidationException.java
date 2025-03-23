package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class CheckBjNicknameValidationException extends RuntimeException {
	private final int code;
	private final String error;

	public CheckBjNicknameValidationException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
