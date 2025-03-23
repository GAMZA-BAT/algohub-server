package com.gamzabat.algohub.feature.comment.exception;

import lombok.Getter;

@Getter
public class CommentValidationException extends RuntimeException {
	private final int code;
	private final String error;

	public CommentValidationException(int code, String error) {
		this.code = code;
		this.error = error;
	}
}
