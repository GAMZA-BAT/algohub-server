package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class InvalidEmailException extends RuntimeException {
	private final String errors;

	public InvalidEmailException(String errors) {
		this.errors = errors;
	}
}
