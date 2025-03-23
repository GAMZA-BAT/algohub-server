package com.gamzabat.algohub.feature.solution.dto;

import org.springframework.data.domain.Page;

public record GetMySolutionListResponse(Page<GetSolutionResponse> inProgress,
										Page<GetSolutionResponse> expired) {
}
