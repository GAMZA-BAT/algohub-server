package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(@NotBlank(message = "이메일(닉네임)은 필수 입력 입니다.") String identifier,
							@NotBlank(message = "비밀번호는 필수 입력 입니다.") String password) {
}
