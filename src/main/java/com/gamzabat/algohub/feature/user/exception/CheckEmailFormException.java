package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class CheckEmailFormException extends RuntimeException {
	private final String errors;

	public CheckEmailFormException(String errors) {
		this.errors = errors;
	}
}
