package com.gamzabat.algohub.feature.problem.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {
	private final HttpStatus status;
	private final String error;

	public InvalidRequestException(HttpStatus status, String error) {
		this.status = status;
		this.error = error;
	}
}