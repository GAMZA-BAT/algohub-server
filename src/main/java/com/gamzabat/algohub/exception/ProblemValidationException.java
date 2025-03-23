package com.gamzabat.algohub.exception;

import lombok.Getter;

@Getter
public class ProblemValidationException extends RuntimeException {
	private final int code;
	private final String error;

	public ProblemValidationException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
