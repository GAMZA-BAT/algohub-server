package com.gamzabat.algohub.feature.user.exception;

import lombok.Getter;

@Getter
public class UncorrectedPasswordException extends RuntimeException {
	private final String errors;

	public UncorrectedPasswordException(String errors) {
		this.errors = errors;
	}
}
