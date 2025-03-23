package com.gamzabat.algohub.exception;

import java.util.ArrayList;

public record ErrorResponse(int status,
							String error,
							ArrayList<String> messages) {
}
