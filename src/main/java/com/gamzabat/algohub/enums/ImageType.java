package com.gamzabat.algohub.enums;

public enum ImageType {
	USER("user"),
	GROUP("group");

	private final String value;

	ImageType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
