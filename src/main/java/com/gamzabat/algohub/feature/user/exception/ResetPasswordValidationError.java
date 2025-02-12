package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class ResetPasswordValidationError extends RuntimeException {
	private final int code;
	private final String errors;

	public ResetPasswordValidationError(int code, String errors) {
		this.code = code;
		this.errors = errors;
	}
}
