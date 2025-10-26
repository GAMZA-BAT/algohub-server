package com.gamzabat.algohub.feature.recommendation.dto;

import com.gamzabat.algohub.feature.recommendation.domain.TagType;

public record RecommendationItemDto(
	TagType tagType,
	Double score,
	StudyGroupSummaryDto studyGroup
) {
}
