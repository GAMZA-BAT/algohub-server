package com.gamzabat.algohub.feature.studygroup.dto;

import lombok.Getter;

@Getter
public class CheckSolvedProblemResponse {
	private final Long id;
	private final String nickname;
	private final String profileImage;
	private final Boolean solved;

	public CheckSolvedProblemResponse(Long id, String profileImage, String nickname, Boolean solved) {
		this.id = id;
		this.profileImage = profileImage;
		this.nickname = nickname;
		this.solved = solved;
	}
}
