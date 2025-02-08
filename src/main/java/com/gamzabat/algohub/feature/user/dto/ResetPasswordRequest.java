package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
	@NotBlank String token,
	@NotBlank String password) {
}
