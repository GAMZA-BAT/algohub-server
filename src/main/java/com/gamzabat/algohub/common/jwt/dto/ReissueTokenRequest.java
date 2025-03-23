package com.gamzabat.algohub.common.jwt.dto;

public record ReissueTokenRequest(String expiredAccessToken, String refreshToken) {
}
