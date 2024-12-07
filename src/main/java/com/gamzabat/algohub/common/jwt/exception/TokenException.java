package com.gamzabat.algohub.common.jwt.exception;

import lombok.Getter;

@Getter
public class TokenException extends RuntimeException {
	private final int code;
	private final String error;

	public TokenException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
