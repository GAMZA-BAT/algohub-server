package com.gamzabat.algohub.feature.edgecase.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class NotAuthorizedUserException extends RuntimeException {
	private final String error;
	private final HttpStatus httpStatus;

	public NotAuthorizedUserException(String error, HttpStatus httpStatus) {
		this.error = error;
		this.httpStatus = httpStatus;
	}
}
