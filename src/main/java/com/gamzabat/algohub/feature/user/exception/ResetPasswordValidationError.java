package com.gamzabat.algohub.feature.user.exception;

public class ResetPasswordValidationError extends RuntimeException {
	public ResetPasswordValidationError(String message) {
		super(message);
	}
}
