package com.gamzabat.algohub.feature.group.ranking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRankingResponse {
	private String userNickname;
	private String profileImage;
	private Integer rank;
	private Long solvedCount;
	private String rankDiff;

	public GetRankingResponse(String userNickname, String profileImage, Integer rank, Long solvedCount,
		String rankDiff) {
		this.userNickname = userNickname;
		this.profileImage = profileImage;
		this.rank = rank;
		this.solvedCount = solvedCount;
		this.rankDiff = rankDiff;
	}
}
