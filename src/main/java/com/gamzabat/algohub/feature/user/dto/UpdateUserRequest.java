package com.gamzabat.algohub.feature.user.dto;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
	private String nickname;
	private String bjNickname;

	public UpdateUserRequest(String nickname, String bjNickname) {
		this.nickname = nickname;
		this.bjNickname = bjNickname;
	}
}
