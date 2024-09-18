package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotBlank;

public record EditUserPasswordRequest(@NotBlank(message = "기존 비밀번호는 필수 입력입니다.") String currentPassword,
                                      @NotBlank(message = "새로운 비밀번호는 필수 입력입니다.") String newPassword) {
}