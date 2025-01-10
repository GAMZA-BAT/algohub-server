package com.gamzabat.algohub.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthAccessTokenDto {
	@JsonProperty("access_token")
	private String accessToken;
}
