package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotNull;

public record DeleteUserRequest(@NotNull(message = "소셜 로그인 여부는 필수 입력입니다.") Boolean isOAuthAccount,
								String password) {
}
