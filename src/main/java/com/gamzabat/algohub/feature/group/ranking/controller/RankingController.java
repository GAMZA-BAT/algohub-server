package com.gamzabat.algohub.feature.group.ranking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/api/groups")
@Tag(name = "랭킹 관련 API", description = "랭킹 조회 관련 API")
public class RankingController {
	private final RankingService rankingService;

	@GetMapping(value = "/{groupId}/rankings")
	@Operation(summary = "과제 진행도 전체순위 API")
	public ResponseEntity<List<GetRankingResponse>> getAllRanking(@AuthedUser User user, @PathVariable Long groupId) {
		List<GetRankingResponse> rankingResponse = rankingService.getAllRank(user, groupId);
		return ResponseEntity.ok().body(rankingResponse);
	}
}
