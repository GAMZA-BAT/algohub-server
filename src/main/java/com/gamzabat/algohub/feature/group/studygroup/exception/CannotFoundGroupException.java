package com.gamzabat.algohub.feature.group.studygroup.exception;

import lombok.Getter;

@Getter
public class CannotFoundGroupException extends RuntimeException {
	private final String errors;

	public CannotFoundGroupException(String errors) {
		this.errors = errors;
	}
}
