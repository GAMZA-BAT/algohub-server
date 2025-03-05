package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class InvalidVerificationCodeException extends RuntimeException {
	private final String errors;

	public InvalidVerificationCodeException(String errors) {
		this.errors = errors;
	}
}
