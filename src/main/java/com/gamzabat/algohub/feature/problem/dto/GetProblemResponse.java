package com.gamzabat.algohub.feature.problem.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.common.DateFormatUtil;

import lombok.Getter;

@Getter
public class GetProblemResponse {
	private String title;
	private Long problemId;
	private String link;
	private String startDate;
	private String endDate;
	private Integer level;
	private boolean solved;
	private Integer submitMemberCount;
	private Integer memberCount;
	private Integer accurancy;
	private boolean inProgress;

	public GetProblemResponse(String title, Long problemId, String link, LocalDate startDate, LocalDate endDate,
		Integer level, boolean solved, Integer submissionCount, Integer memberCount, Integer accurancy,
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
		this.accurancy = accurancy;
		this.inProgress = inProgress;
	}
}
