package com.gamzabat.algohub.feature.problem.exception;

import lombok.Getter;

@Getter
public class SolvedAcApiErrorException extends RuntimeException {
	private final int code;
	private final String error;

	public SolvedAcApiErrorException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
