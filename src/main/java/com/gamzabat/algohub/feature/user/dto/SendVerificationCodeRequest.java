package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotBlank;

public record SendVerificationCodeRequest(@NotBlank(message = "이메일은 필수 입력 입니다.") String email) {
}
