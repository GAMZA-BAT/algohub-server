package com.gamzabat.algohub.feature.solution.dto;

import org.springframework.web.bind.annotation.RequestParam;

import lombok.Builder;

@Builder
public record GetSolutionListRequest( String language,
									  String result,
									  String nickname) {
}
