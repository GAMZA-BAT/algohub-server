package com.gamzabat.algohub.feature.problem.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.common.DateFormatUtil;

import lombok.Getter;

@Getter
public class GetProblemResponse {
	private final String title;
	private final Long problemId;
	private final String link;
	private final String startDate;
	private final String endDate;
	private final Integer level;
	private final boolean solved;
	private final Integer submitMemberCount;
	private final Integer memberCount;
	private final Integer accuracy;
	private final boolean inProgress;

	public GetProblemResponse(String title, Long problemId, String link, LocalDate startDate, LocalDate endDate,
		Integer level, boolean solved, Integer submissionCount, Integer memberCount, Integer accuracy,
		boolean inProgress) {
		this.title = title;
		this.problemId = problemId;
		this.link = link;
		this.startDate = DateFormatUtil.formatDate(startDate);
		this.endDate = DateFormatUtil.formatDate(endDate);
		this.level = level;
		this.solved = solved;
		this.submitMemberCount = submissionCount;
		this.memberCount = memberCount;
		this.accuracy = accuracy;
		this.inProgress = inProgress;
	}
}