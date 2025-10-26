package com.gamzabat.algohub.feature.recommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.recommendation.dto.HomeRecommendationsResponse;
import com.gamzabat.algohub.feature.recommendation.service.RecommendationService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "추천 API", description = "홈 화면 추천 스터디 API")
public class RecommendationController {

	private final RecommendationService recommendationService;

	@GetMapping("/home/recommendations")
	@Operation(summary = "홈 추천 스터디 조회 API")
	public ResponseEntity<HomeRecommendationsResponse> getHomeRecommendations(
		@AuthedUser User user
	) {
		HomeRecommendationsResponse response = recommendationService.getHomeRecommendations(user.getId());
		return ResponseEntity.ok().body(response);
	}
}
