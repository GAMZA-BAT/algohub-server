package com.gamzabat.algohub.feature.problem.dto;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record EditProblemRequest(LocalDate startDate,
								 LocalDate endDate) {
}
