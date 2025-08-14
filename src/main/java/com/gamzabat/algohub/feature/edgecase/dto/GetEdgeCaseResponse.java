package com.gamzabat.algohub.feature.edgecase.dto;

import lombok.Getter;

@Getter
public class GetEdgeCaseResponse {
	private final Integer edgeCaseId;
	private final Integer level;
	private final Integer problemNumber;
	private final String title;
	private final String input;
	private final String output;
	private final Integer like;

	public GetEdgeCaseResponse(Integer edgeCaseId, Integer level, Integer problemNumber, String title, String input, String output, Integer like) {
		this.edgeCaseId = edgeCaseId;
		this.level = level;
		this.problemNumber = problemNumber;
		this.title = title;
		this.input = input;
		this.output = output;
		this.like = like;
	}
}
