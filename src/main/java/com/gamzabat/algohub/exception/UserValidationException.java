package com.gamzabat.algohub.exception;

import lombok.Getter;

@Getter
public class UserValidationException extends RuntimeException {
	private final String errors;

	public UserValidationException(String errors) {
		this.errors = errors;
	}
}
