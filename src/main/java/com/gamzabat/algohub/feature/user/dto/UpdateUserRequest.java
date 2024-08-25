package com.gamzabat.algohub.feature.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//TODO: Setter 제거하기
public class UpdateUserRequest {
	private String nickname;
	private String bjNickname;
}
