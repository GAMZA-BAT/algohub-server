package com.gamzabat.algohub.feature.solution.dto;

import lombok.Builder;

@Builder
public record GetSolutionListRequest( String language,
									  String result,
									  String nickname) {
}
