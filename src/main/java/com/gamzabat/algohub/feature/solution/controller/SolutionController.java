package com.gamzabat.algohub.feature.solution.controller;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.solution.dto.CreateSolutionRequest;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
import com.gamzabat.algohub.feature.solution.service.SolutionService;
import com.gamzabat.algohub.feature.user.domain.User;


import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solution")
@Tag(name = "풀이 API", description = "문제 풀이 관련 API")
public class SolutionController {
	private final SolutionService solutionService;

	@GetMapping("/solutions")
	@Operation(summary = "풀이 목록 조회 API", description = "특정 문제에 대한 풀이를 모두 조회하는 API")
	public ResponseEntity<Page<GetSolutionResponse>> getSolutionList(@AuthedUser User user,
																	 @RequestParam Long problemId,
																	 @RequestParam(defaultValue = "0") int page,
																	 @RequestParam(defaultValue = "20") int size)

	{
		Pageable pageable = PageRequest.of(page,size);
		Page<GetSolutionResponse> response = solutionService.getSolutionList(user, problemId, pageable);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/solution")
	@Operation(summary = "풀이 하나 조회 API", description = "특정 풀이 하나를 조회하는 API")
	public ResponseEntity<GetSolutionResponse> getSolution(@AuthedUser User user,
														   @RequestParam Long solutionId)
	{
		GetSolutionResponse response = solutionService.getSolution(user,solutionId);
		return ResponseEntity.ok().body(response);
	}

	@PostMapping
	@Operation(summary = "풀이 생성 API")
	public ResponseEntity<Object> createSolution(@Valid @RequestBody CreateSolutionRequest request, Errors errors){
		if(errors.hasErrors())
			throw new RequestException("풀이 생성 요청이 올바르지 않습니다.",errors);
		solutionService.createSolution(request);
		return ResponseEntity.ok().body("OK");
	}

}
