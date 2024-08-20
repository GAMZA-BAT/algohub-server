package com.gamzabat.algohub.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserInfoResponse {
	private String email;
	private String nickname;
	private String profileImage;
	private String bjNickname;
}
