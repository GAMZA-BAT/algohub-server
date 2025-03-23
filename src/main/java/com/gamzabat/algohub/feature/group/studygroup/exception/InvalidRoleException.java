package com.gamzabat.algohub.feature.group.studygroup.exception;

import lombok.Getter;

@Getter
public class InvalidRoleException extends RuntimeException {
	private final int code;
	private final String error;

	public InvalidRoleException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
