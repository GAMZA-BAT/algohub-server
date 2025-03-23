package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckEmailRequest(
	@NotBlank(message = "이메일은 필수 입력 입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email) {
}
