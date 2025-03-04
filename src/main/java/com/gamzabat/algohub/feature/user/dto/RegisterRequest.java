package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank(message = "이메일은 필수 입력입니다.") String email,
							  @NotBlank(message = "비밀번호는 필수 입력입니다.") String password,
							  @NotBlank(message = "닉네임은 필수 입력입니다.") String nickname) {
}
