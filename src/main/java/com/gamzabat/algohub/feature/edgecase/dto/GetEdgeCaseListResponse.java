package com.gamzabat.algohub.feature.edgecase.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record GetEdgeCaseListResponse(
	List<GetEdgeCaseResponse> edgeCaseList) {
}
