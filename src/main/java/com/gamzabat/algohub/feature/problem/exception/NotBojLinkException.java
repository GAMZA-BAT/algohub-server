package com.gamzabat.algohub.feature.problem.exception;

import lombok.Getter;

@Getter
public class NotBojLinkException extends RuntimeException {
	private final String error;
	private final int code;

	public NotBojLinkException(int code, String error) {
		this.code = code;
		this.error = error;
	}

}
