package com.gamzabat.algohub.feature.studygroup.exception;

import lombok.Getter;

@Getter
public class CannotFoundRankingException extends RuntimeException {
	private final String error;

	public CannotFoundRankingException(String error) {
		this.error = error;
	}
}