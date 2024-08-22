package com.gamzabat.algohub.feature.studygroup.etc;

public enum RoleOfGroupMember {
	ADMIN("ADMIN"),
	PARTICIPANT("PARTICIPANT");

	private final String value;

	RoleOfGroupMember(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
