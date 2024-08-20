package com.gamzabat.algohub.feature.studygroup.exception;

import lombok.Getter;

@Getter
public class CannotFoundUserException extends RuntimeException {
	private final int code;
	private final String error;

	public CannotFoundUserException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
