package com.gamzabat.algohub.feature.solution.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.solution.dto.CreateSolutionRequest;
import com.gamzabat.algohub.feature.solution.dto.GetCurrentSolvingStatusResponse;
import com.gamzabat.algohub.feature.solution.dto.GetMySolutionListRequest;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionListRequest;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
import com.gamzabat.algohub.feature.solution.enums.ProgressCategory;
import com.gamzabat.algohub.feature.solution.service.SolutionService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "풀이 API", description = "문제 풀이 관련 API")
public class SolutionController {
	private final SolutionService solutionService;

	@GetMapping("/problems/{problemId}/solutions")
	@Operation(summary = "풀이 목록 조회 API", description = "특정 문제에 대한 풀이를 모두 조회하는 API")
	public ResponseEntity<Page<GetSolutionResponse>> getSolutionList(@AuthedUser User user,
		@PathVariable Long problemId,
		@ModelAttribute GetSolutionListRequest request,
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<GetSolutionResponse> response = solutionService.getSolutionList(user, problemId,request,pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/solutions/{solutionId}")
	@Operation(summary = "풀이 하나 조회 API", description = "특정 풀이 하나를 조회하는 API")
	public ResponseEntity<GetSolutionResponse> getSolution(@AuthedUser User user,
		@PathVariable Long solutionId) {
		GetSolutionResponse response = solutionService.getSolution(user, solutionId);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping("/solutions")
	@Operation(summary = "풀이 생성 API")
	public ResponseEntity<Void> createSolution(@Valid @RequestBody CreateSolutionRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("풀이 생성 요청이 올바르지 않습니다.", errors);
		solutionService.createSolution(request);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/groups/{groupId}/solutions/current-status")
	@Operation(summary = "풀이 현황 테이블 조회 API", description = "진행 중인 문제들에 대해 풀이 현황 테이블을 조회하는 API")
	public ResponseEntity<List<GetCurrentSolvingStatusResponse>> getCurrentSolvingStatus(@AuthedUser User user,
		@PathVariable Long groupId) {
		List<GetCurrentSolvingStatusResponse> response = solutionService.getCurrentSolvingStatuses(user, groupId);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/solutions/me")
	@Operation(summary = "내 풀이 전체 조회", description = "나의 풀이를 그룹, 문제 번호, 언어, 결과, 상태 등으로 필터링하여 조회하는 API")
	public ResponseEntity<Page<GetSolutionResponse>> getMySolutionList(@AuthedUser User user,
		@ModelAttribute GetMySolutionListRequest request,
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<GetSolutionResponse> response = solutionService.getMySolutionList(user, request.groupId(),
			request.problemNumber(), request.language(), request.result(), request.status(), request.isIncorrect(), pageable);
		return ResponseEntity.ok().body(response);
	}
}
