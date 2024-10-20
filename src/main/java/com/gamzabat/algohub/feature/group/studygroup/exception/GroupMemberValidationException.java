package com.gamzabat.algohub.feature.group.studygroup.exception;

import lombok.Getter;

@Getter
public class GroupMemberValidationException extends RuntimeException {
	private final int code;
	private final String error;

	public GroupMemberValidationException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
