package com.gamzabat.algohub.feature.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateUserRequest {
	private final String nickname;
	private final String bjNickname;
	private final String description;
	@NotNull(message = "기본 이미지 여부는 필수 입니다")
	private final Boolean isDefaultImage;

	public UpdateUserRequest(String nickname, String bjNickname, String description, Boolean isDefaultImage) {
		this.nickname = nickname;
		this.bjNickname = bjNickname;
		this.description = description;
		this.isDefaultImage = isDefaultImage;
	}
}
