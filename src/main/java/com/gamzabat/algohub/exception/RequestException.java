package com.gamzabat.algohub.exception;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.validation.Errors;

import lombok.Getter;

@Getter
public class RequestException extends RuntimeException {
	private final String error;
	private final ArrayList<String> messages;

	public RequestException(String error, Errors errors) {
		this.error = error;
		this.messages = errors.getFieldErrors().stream()
			.map(e -> e.getField() + " : " + e.getDefaultMessage())
			.collect(Collectors.toCollection(ArrayList::new));
	}
}
