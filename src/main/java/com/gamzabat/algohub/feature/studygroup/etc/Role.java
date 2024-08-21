package com.gamzabat.algohub.feature.studygroup.etc;

import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.feature.studygroup.exception.InvalidRoleException;

public enum Role {
	ADMIN("ADMIN"),
	PARTICIPANT("PARTICIPANT");

	private final String value;

	Role(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Role fromValue(String value) {
		for (Role role : Role.values()) {
			if (role.value.equals(value)) {
				return role;
			}
		}
		throw new InvalidRoleException(HttpStatus.BAD_REQUEST.value(), "해당 ROLE은 존재하지 않습니다.");
	}
}
