package com.gamzabat.algohub.feature.edgecase.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class GetEdgeCaseListResponse {
	private final List<GetEdgeCaseResponse> edgeCaseList;

	public GetEdgeCaseListResponse(List<GetEdgeCaseResponse> edgeCaseList) {
		this.edgeCaseList = edgeCaseList;
	}
}
