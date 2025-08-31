package com.gamzabat.algohub.enums;

public enum JoinRequestStatus {
	PENDING("pending"),
	APPROVE("approve"),
	CANCEL("cancel"),
	REJECT("reject");
	private String value;

	private JoinRequestStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
