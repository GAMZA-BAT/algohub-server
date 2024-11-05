package com.gamzabat.algohub.feature.board.exception;

import lombok.Getter;

@Getter
public class BoardValidationException extends RuntimeException {
	private final String error;

	public BoardValidationException(String error) {
		this.error = error;
	}
}
