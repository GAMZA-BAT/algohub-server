package com.gamzabat.algohub.enums;

import lombok.Getter;

@Getter
public enum EmailType {
	RESET_PASSWORD("resetPassword"),
	EMAIL_VALIDATION("emailValidation");

	private final String value;

	EmailType(String value) {
		this.value = value;
	}
}
