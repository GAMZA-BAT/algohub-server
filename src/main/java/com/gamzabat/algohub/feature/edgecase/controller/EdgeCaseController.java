package com.gamzabat.algohub.feature.edgecase.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCaseLike;
import com.gamzabat.algohub.feature.edgecase.dto.CreateEdgeCaseRequest;
import com.gamzabat.algohub.feature.edgecase.dto.GetEdgeCaseListResponse;
import com.gamzabat.algohub.feature.edgecase.service.EdgeCaseService;
import com.gamzabat.algohub.feature.user.domain.User;

import org.springframework.validation.Errors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "반례 게시판 API", description = "반례 게시판 관련 API")
public class EdgeCaseController {
	private final EdgeCaseService edgeCaseService;

	@PostMapping("/edge-case")
	@Operation(summary = "반례 등록")
	public ResponseEntity<Void> createEdgeCase(@AuthedUser User user, @RequestBody @Valid CreateEdgeCaseRequest creatEdgeCaseRequest,
		Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 요청입니다.", errors);

		edgeCaseService.createEdgeCase(user, creatEdgeCaseRequest);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/edge-case/list")
	@Operation(summary = "반례리스트 조회")
	public ResponseEntity<GetEdgeCaseListResponse> getEdgeCaseList(@AuthedUser User user, @RequestParam(required = false) Integer problemId) {
		GetEdgeCaseListResponse response = edgeCaseService.getEdgeCaseList(problemId);

		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping("/edge-case/{edgeCaseId}")
	@Operation(summary = "반례 삭제")
	public ResponseEntity<Void> deleteEdgeCase(@AuthedUser User user, @PathVariable Long edgeCaseId) {
		edgeCaseService.deleteEdgeCase(user, edgeCaseId);

		return ResponseEntity.ok().build();
	}

	@PostMapping("/edge-case/like/{edgeCaseId}")
	@Operation(summary = "반례 좋아요 추가")
	public ResponseEntity<Void> addEdgeCaseLike(@AuthedUser User user, @PathVariable Long edgeCaseId) {
		edgeCaseService.addEdgeCaseLike(user, edgeCaseId);
		return ResponseEntity.ok().build();
	}
}
