package com.gamzabat.algohub.feature.solution.dto;

import org.springframework.web.bind.annotation.RequestParam;

import com.gamzabat.algohub.feature.solution.enums.ProgressCategory;

import lombok.Builder;

@Builder
public record GetMySolutionListRequest(Long groupId,
									   Integer problemNumber,
									   String language,
									   String result,
									   ProgressCategory status,
									   Boolean isIncorrect) {
}
