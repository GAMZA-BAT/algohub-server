package com.gamzabat.algohub.feature.problem.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "문제 API", description = "그룹별 문제 관련 API")

public class ProblemController {
	private final ProblemService problemService;
	private final String PROBLEM_SORT_BY = "startDate";

	@PostMapping(value = "/groups/{groupId}/problems")
	@Operation(summary = "문제 생성 API")
	public ResponseEntity<Void> createProblem(@AuthedUser User user,
		@PathVariable Long groupId,
		@Valid @RequestBody CreateProblemRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("문제 생성 요청이 올바르지 않습니다.", errors);
		problemService.createProblem(user, groupId, request);
		return ResponseEntity.ok().build();
	}

	@PatchMapping(value = "/problems/{problemId}")
	@Operation(summary = "문제 마감 기한 수정 API")
	public ResponseEntity<Void> editProblemDeadline(@AuthedUser User user,
		@PathVariable Long problemId,
		@Valid @RequestBody EditProblemRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("문제 마감 기한 수정 요청이 올바르지 않습니다.", errors);
		problemService.editProblem(user, problemId, request);
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/groups/{groupId}/problems/in-progress")
	@Operation(summary = "진행 중인 문제 목록 조회 API", description = "특정 그룹에 대한 문제를 모두 조회하는 API")
	public ResponseEntity<Page<GetProblemResponse>> getInProgressProblemList(@AuthedUser User user,
		@PathVariable Long groupId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(PROBLEM_SORT_BY).descending());
		Page<GetProblemResponse> response = problemService.getInProgressProblems(user, groupId, pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping(value = "/groups/{groupId}/problems/expired")
	@Operation(summary = "마감 된 문제 목록 조회 API", description = "특정 그룹에 대한 문제를 모두 조회하는 API")
	public ResponseEntity<Page<GetProblemResponse>> getExpiredProblemList(@AuthedUser User user,
		@PathVariable Long groupId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(PROBLEM_SORT_BY).descending());
		Page<GetProblemResponse> response = problemService.getExpiredProblems(user, groupId, pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/problems/{problemId}")
	@Operation(summary = "문제 단건 조회 API")
	public ResponseEntity<GetProblemResponse> getProblem(@AuthedUser User user, @PathVariable Long problemId) {
		GetProblemResponse response = problemService.getProblem(user, problemId);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/groups/{groupId}/problems/deadline-reached")
	@Operation(summary = "마감 기한이 내일까지인 문제들 조회 API")
	public ResponseEntity<List<GetProblemResponse>> getDeadlineReachedProblemList(@AuthedUser User user,
		@PathVariable Long groupId) {
		return ResponseEntity.ok().body(problemService.getDeadlineReachedProblemList(user, groupId));
	}

	@GetMapping("/groups/{groupId}/problems/queued")
	@Operation(summary = "시작 예정인 문제들 조회 API")
	public ResponseEntity<Page<GetProblemResponse>> getQueuedProblemList(@AuthedUser User user,
		@PathVariable Long groupId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(PROBLEM_SORT_BY).descending());
		return ResponseEntity.ok().body(problemService.getQueuedProblems(user, groupId, pageable));
	}

	@DeleteMapping(value = "/problems/{problemId}")
	@Operation(summary = "문제 삭제 API")
	public ResponseEntity<Void> deleteProblem(@AuthedUser User user, @PathVariable Long problemId) {
		problemService.deleteProblem(user, problemId);
		return ResponseEntity.ok().build();
	}
}
