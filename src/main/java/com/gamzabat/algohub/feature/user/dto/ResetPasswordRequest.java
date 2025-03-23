package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
	@NotBlank(message = "비밀번호 재설정 토큰은 필수 항목입니다.") String token,
	@NotBlank(message = "변경할 비밀번호는 필수 항목입니다.") String password) {
}
