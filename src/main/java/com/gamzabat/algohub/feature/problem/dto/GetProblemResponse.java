package com.gamzabat.algohub.feature.problem.dto;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class GetProblemResponse {
	private final String title;
	private final Long problemId;
	private final String link;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final Integer level;
	private final boolean solved;
	private final Integer submitMemberCount;
	private final Integer memberCount;
	private final Integer accurancy;
	private final boolean inProgress;

	public GetProblemResponse(String title, Long problemId, String link, LocalDate startDate, LocalDate endDate,
		Integer level, boolean solved, Integer submissionCount, Integer memberCount, Integer accurancy,
		boolean inProgress) {
		this.title = title;
		this.problemId = problemId;
		this.link = link;
		this.startDate = startDate;
		this.endDate = endDate;
		this.level = level;
		this.solved = solved;
		this.submitMemberCount = submissionCount;
		this.memberCount = memberCount;
		this.accurancy = accurancy;
		this.inProgress = inProgress;
	}
}
/*public record GetProblemResponse(Long problemId,
								 String link,
								 String title,
								 LocalDate deadline,
								 Integer level,
								 Integer submissionCount,
								 Integer memberCount,
								 Integer accurancy) {
	public static GetProblemResponse toDTO(Problem problem){
		return GetProblemResponse.builder()
			.problemId(problem.getId())
			.link(problem.getLink())
			.title(problem.getTitle())
			.deadline(problem.getDeadline())
			.level(problem.getLevel())
			.build();
	}
}*/
