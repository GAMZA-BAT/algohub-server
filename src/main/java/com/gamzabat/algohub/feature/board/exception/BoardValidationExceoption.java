package com.gamzabat.algohub.feature.board.exception;

import lombok.Getter;

@Getter
public class BoardValidationExceoption extends RuntimeException {
	private final String error;

	public BoardValidationExceoption(String error) {
		this.error = error;
	}
}
