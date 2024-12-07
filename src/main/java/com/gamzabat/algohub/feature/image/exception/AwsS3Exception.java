package com.gamzabat.algohub.feature.image.exception;

import lombok.Getter;

@Getter
public class AwsS3Exception extends RuntimeException {
	private final String error;

	public AwsS3Exception(String error) {
		this.error = error;
	}
}
