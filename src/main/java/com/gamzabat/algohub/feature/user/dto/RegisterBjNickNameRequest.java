package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterBjNickNameRequest(@NotNull(message = "백준 아이디는 필수 입력입니다") String bjNickName) {
}
