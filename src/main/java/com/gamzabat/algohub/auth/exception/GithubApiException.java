package com.gamzabat.algohub.auth.exception;

import lombok.Getter;

@Getter
public class GithubApiException extends RuntimeException {
	public GithubApiException(String message) {
		super(message);
	}
}
