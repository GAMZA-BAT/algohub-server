package com.gamzabat.algohub.feature.user.dto;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
	private final String nickname;
	private final String bjNickname;
	private final String description;

	public UpdateUserRequest(String nickname, String bjNickname, String description) {
		this.nickname = nickname;
		this.bjNickname = bjNickname;
		this.description = description;
	}
}
