package com.gamzabat.algohub.feature.solution.exception;

import lombok.Getter;

@Getter
public class CannotFoundSolutionException extends RuntimeException{
    private final String errors;

	public CannotFoundSolutionException(String errors) {
		this.errors = errors;
	}
}
