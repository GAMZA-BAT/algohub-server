package com.gamzabat.algohub.feature.problem.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class GetProblemListsResponse {
	private final List<GetProblemResponse> inProgressProblems;
	private final List<GetProblemResponse> expiredProblems;
	private final int currentPage;
	private final int totalPages;
	private final long totalItems;

	public GetProblemListsResponse(List<GetProblemResponse> inProgressProblems,
		List<GetProblemResponse> completedProblems, int currentPage, int totalPages, long totalItems) {
		this.inProgressProblems = inProgressProblems;
		this.expiredProblems = completedProblems;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.totalItems = totalItems;
	}

}
