package com.gamzabat.algohub.feature.user.exception;

public class InvalidDeleteUserRequestException extends RuntimeException {
	public InvalidDeleteUserRequestException(final String message) {
		super(message);
	}
}
