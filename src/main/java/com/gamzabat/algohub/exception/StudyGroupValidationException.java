package com.gamzabat.algohub.exception;

import lombok.Getter;

@Getter
public class StudyGroupValidationException extends RuntimeException {
	private final int code;
	private final String error;

	public StudyGroupValidationException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
