package com.gamzabat.algohub.feature.edgecase.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AlreadyLikedException extends RuntimeException {
	private final String errors;
	private final HttpStatus httpStatus;

	public AlreadyLikedException(String errors, HttpStatus httpStatus) {
		this.errors = errors;
		this.httpStatus = httpStatus;
	}
}
