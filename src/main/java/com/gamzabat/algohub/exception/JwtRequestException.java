package com.gamzabat.algohub.exception;

import java.util.ArrayList;

public class JwtRequestException extends RuntimeException {
	private final Integer code;
	private final String error;
	private final ArrayList<String> messages = new ArrayList<>();

	public JwtRequestException(Integer code, String error, String message) {
		this.code = code;
		this.error = error;
		this.messages.add(message);
	}

	public Integer getCode() {
		return code;
	}

	public String getError() {
		return error;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}
}
