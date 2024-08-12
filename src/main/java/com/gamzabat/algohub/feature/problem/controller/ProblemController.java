package com.gamzabat.algohub.feature.problem.controller;

import java.util.List;

import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemListsResponse;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.exception.RequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
@Tag(name = "문제 API", description = "그룹별 문제 관련 API")

public class ProblemController {
	private final ProblemService problemService;

	@PostMapping
	@Operation(summary = "문제 생성 API")
	public ResponseEntity<Object> createProblem(@AuthedUser User user,
												@Valid @RequestBody CreateProblemRequest request, Errors errors){
		if(errors.hasErrors())
			throw new RequestException("문제 생성 요청이 올바르지 않습니다.",errors);
		problemService.createProblem(user, request);
		return ResponseEntity.ok().body("OK");
	}

	@PatchMapping
	@Operation(summary = "문제 마감 기한 수정 API")
	public ResponseEntity<Object> editProblemDeadline(@AuthedUser User user,
													  @Valid @RequestBody EditProblemRequest request, Errors errors){
		if(errors.hasErrors())
			throw new RequestException("문제 마감 기한 수정 요청이 올바르지 않습니다.",errors);
		problemService.editProblem(user, request);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping
	@Operation(summary = "문제 조회 API", description = "특정 그룹에 대한 문제를 모두 조회하는 API")
	public ResponseEntity<GetProblemListsResponse> getProblemList(@AuthedUser User user,
																  @RequestParam Long groupId,
																  @RequestParam(defaultValue = "0") int page,
																  @RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size);
		GetProblemListsResponse response = problemService.getProblemList(user, groupId, pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/deadline-reached")
	@Operation(summary = "마감 기한이 오늘까지인 문제들 조회 API")
	public ResponseEntity<List<GetProblemResponse>> getDeadlineReachedProblemList(@AuthedUser User user, @RequestParam Long groupId){
		return ResponseEntity.ok().body(problemService.getDeadlineReachedProblemList(user,groupId));
	}

	@DeleteMapping
	@Operation(summary = "문제 삭제 API")
	public ResponseEntity<Object> deleteProblem(@AuthedUser User user, @RequestParam Long problemId){
		problemService.deleteProblem(user,problemId);
		return ResponseEntity.ok().body("OK");
	}
}
