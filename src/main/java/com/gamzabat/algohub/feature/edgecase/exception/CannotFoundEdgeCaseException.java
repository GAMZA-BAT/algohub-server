package com.gamzabat.algohub.feature.edgecase.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CannotFoundEdgeCaseException extends RuntimeException {
	private final String errors;
	private final HttpStatus httpStatus;

	public CannotFoundEdgeCaseException(String errors, HttpStatus httpStatus) {
		this.errors = errors;
		this.httpStatus = httpStatus;
	}
}
