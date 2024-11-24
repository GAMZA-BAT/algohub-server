package com.gamzabat.algohub.feature.solution.dto;

import org.springframework.data.domain.Page;

public record GetMySolutionListWithGroupIdResponse(Page<GetSolutionWithGroupIdResponse> inProgress,
												   Page<GetSolutionWithGroupIdResponse> expired) {
}
