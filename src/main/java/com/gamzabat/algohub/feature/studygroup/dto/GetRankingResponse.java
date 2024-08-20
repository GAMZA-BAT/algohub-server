package com.gamzabat.algohub.feature.studygroup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRankingResponse {
	private String userNickname;
	private String profileImage;
	private Integer rank;
	private Long solvedCount;

	public GetRankingResponse(String userNickname, String profileImage, Integer rank, Long solvedCount) {
		this.userNickname = userNickname;
		this.profileImage = profileImage;
		this.rank = rank;
		this.solvedCount = solvedCount;
	}
}
