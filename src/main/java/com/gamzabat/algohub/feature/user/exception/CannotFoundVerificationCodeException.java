package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class CannotFoundVerificationCodeException extends RuntimeException {
	private final String errors;

	public CannotFoundVerificationCodeException(String errors) {
		this.errors = errors;
	}
}
