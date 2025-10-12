package com.gamzabat.algohub.feature.recommendation.dto;

import java.util.List;

public record StudyGroupSummaryDto(
        Long id,
        String name,
        String introduction,
        String groupImage,
        List<String> tags
) {}
