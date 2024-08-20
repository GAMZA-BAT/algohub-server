package com.gamzabat.algohub.feature.studygroup.dto;

import lombok.Getter;

@Getter
public class CheckSolvedProblemResponse {
	private Long id;
	private String nickname;
	private String profileImage;
	private Boolean solved;

	public CheckSolvedProblemResponse(Long id, String profileImage, String nickname, Boolean solved) {
		this.id = id;
		this.profileImage = profileImage;
		this.nickname = nickname;
		this.solved = solved;
	}
}
