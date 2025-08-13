package com.gamzabat.algohub.feature.edgecase.dto;

import lombok.Getter;

@Getter
public class GetEdgeCaseResponse {
	private final Integer id;
	private final Integer level;
	private final Integer number;
	private final String title;
	private final String input;
	private final String output;
	private final Integer like;

	public GetEdgeCaseResponse(Integer id, Integer level, Integer number, String title, String input, String output, Integer like) {
		this.id = id;
		this.level = level;
		this.number = number;
		this.title = title;
		this.input = input;
		this.output = output;
		this.like = like;
	}
}
