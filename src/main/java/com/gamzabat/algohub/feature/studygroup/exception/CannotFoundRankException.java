package com.gamzabat.algohub.feature.studygroup.exception;

import lombok.Getter;

@Getter
public class CannotFoundRankException extends RuntimeException {
	private final String error;

	public CannotFoundRankException(String error) {
		this.error = error;
	}
}
