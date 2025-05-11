package com.gamzabat.algohub.feature.solution.dto;

import java.util.List;

public record GetCurrentSolvingStatusResponse(Integer rank,
											  String nickname,
											  Integer totalSubmissionCount,
											  String totalPassedTime,
											  List<GetSolvingStatusPerProblemResponse> problems) {
}
