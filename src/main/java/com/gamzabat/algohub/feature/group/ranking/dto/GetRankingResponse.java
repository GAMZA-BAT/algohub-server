package com.gamzabat.algohub.feature.group.ranking.dto;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetRankingResponse {
	private String userNickname;
	private String profileImage;
	private Integer rank;
	private Integer solvedCount;
	private String rankDiff;

	public GetRankingResponse(String userNickname, String profileImage, Integer rank, Integer solvedCount,
		String rankDiff) {
		this.userNickname = userNickname;
		this.profileImage = profileImage;
		this.rank = rank;
		this.solvedCount = solvedCount;
		this.rankDiff = rankDiff;
	}

	public static GetRankingResponse toDTO(Ranking ranking) {
		return new GetRankingResponse(
			ranking.getMember().getUser().getNickname(),
			ranking.getMember().getUser().getProfileImage(),
			ranking.getCurrentRank(),
			ranking.getSolvedCount(),
			ranking.getRankDiff()
		);
	}
}
