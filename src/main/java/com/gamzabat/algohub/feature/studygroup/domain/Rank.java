package com.gamzabat.algohub.feature.studygroup.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Rank {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private GroupMember member;

	private int solvedCount;
	private int currentRank;
	private String rankDiff;

	@Builder
	public Rank(GroupMember member, int solvedCount, int currentRank, String rankDiff) {
		this.member = member;
		this.solvedCount = solvedCount;
		this.currentRank = currentRank;
		this.rankDiff = rankDiff;
	}

	public void increaseSolvedCount() {
		this.solvedCount++;
	}

	public void updateRank(int newRank) {
		this.currentRank = newRank;
	}

	public void updateRankDiff(String newRankDiff) {
		this.rankDiff = newRankDiff;
	}
}
