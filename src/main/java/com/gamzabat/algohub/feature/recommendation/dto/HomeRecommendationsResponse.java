package com.gamzabat.algohub.feature.recommendation.dto;

public record HomeRecommendationsResponse(
        RecommendationItemDto mostActiveThisWeek,
        RecommendationItemDto highJoinRateRecent,
        RecommendationItemDto similarDifficulty
) {}
