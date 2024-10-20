package com.gamzabat.algohub.feature.group.ranking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.group.ranking.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.group.ranking.service.RankingService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
@Tag(name = "랭킹 관련 API", description = "랭킹 조회 관련 API")
public class RankingController {
	private final RankingService rankingService;

	@GetMapping(value = "/top-ranking")
	@Operation(summary = "과제 진행도 상위순위")
	public ResponseEntity<List<GetRankingResponse>> getTopRanking(@AuthedUser User user, @RequestParam Long groupId) {
		List<GetRankingResponse> rankingResponse = rankingService.getTopRank(user, groupId);
		return ResponseEntity.ok().body(rankingResponse);
	}

	@GetMapping(value = "/all-ranking")
	@Operation(summary = "과제 진행도 전체순위")
	public ResponseEntity<List<GetRankingResponse>> getAllRanking(@AuthedUser User user, @RequestParam Long groupId) {
		List<GetRankingResponse> rankingResponse = rankingService.getAllRank(user, groupId);
		return ResponseEntity.ok().body(rankingResponse);
	}
}
