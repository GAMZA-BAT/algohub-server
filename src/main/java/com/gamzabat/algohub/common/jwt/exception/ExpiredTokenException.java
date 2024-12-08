package com.gamzabat.algohub.common.jwt.exception;

import lombok.Getter;

@Getter
public class ExpiredTokenException extends TokenException {
	public ExpiredTokenException(int code, String error) {
		super(code, error);
	}
}

