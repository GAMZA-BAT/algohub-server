package com.gamzabat.algohub.feature.recommendation.dto;

import java.time.LocalDate;
import java.util.List;

public record StudyGroupSummaryDto(
	Long id,
	String name,
	String introduction,
	String groupImage,
	List<String> tags,
	LocalDate startDate,
	LocalDate endDate
) {
}

