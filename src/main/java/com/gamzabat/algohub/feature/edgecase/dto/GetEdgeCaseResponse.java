package com.gamzabat.algohub.feature.edgecase.dto;

import lombok.Builder;

@Builder
public record GetEdgeCaseResponse(
	Integer edgeCaseId,
	Integer level,
	Integer problemNumber,
	String title,
	String input,
	String output,
	Integer like,
	Boolean isLiked) {
}
