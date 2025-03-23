package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class BOJServerErrorException extends RuntimeException {
	private final String error;

	public BOJServerErrorException(String error) {
		this.error = error;
	}
}
