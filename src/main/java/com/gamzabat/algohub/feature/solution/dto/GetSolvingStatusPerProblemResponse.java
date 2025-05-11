package com.gamzabat.algohub.feature.solution.dto;

public record GetSolvingStatusPerProblemResponse(Long problemId,
												 Long firstCorrectSolutionId,
												 int submissionCount,
												 String firstCorrectDuration,
												 boolean solved) {
}
