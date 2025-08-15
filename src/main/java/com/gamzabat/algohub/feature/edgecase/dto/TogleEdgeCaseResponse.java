package com.gamzabat.algohub.feature.edgecase.dto;

import lombok.Getter;

@Getter
public class TogleEdgeCaseResponse {
	private final Boolean islike;

	public TogleEdgeCaseResponse(Boolean islike) {
		this.islike = islike;
	}
}

