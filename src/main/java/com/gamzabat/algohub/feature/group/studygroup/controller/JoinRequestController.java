package com.gamzabat.algohub.feature.group.studygroup.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.group.studygroup.domain.JoinRequest;
import com.gamzabat.algohub.feature.group.studygroup.dto.UpdateJoinRequestStatusRequest;
import com.gamzabat.algohub.feature.group.studygroup.exception.JoinRequestException;
import com.gamzabat.algohub.feature.group.studygroup.service.JoinRequestService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "그룹 가입 요청API", description = "스터디 그룹  가입 요청 관련 API")
public class JoinRequestController {
	private final JoinRequestService joinRequestService;

	@PostMapping(value = "/groups/{groupId}/join-request")
	@Operation(summary = "그룹 가입 요청 API", description = "스터디 그룹에 가입 요청을 보내는 API")
	public ResponseEntity<Void> joinRequest(@AuthedUser User user, @PathVariable Long groupId) {
		joinRequestService.joinRequest(user, groupId);
		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/groups/{groupId}/join-request")
	@Operation(summary = "그룹 가입 요청 목록 조회 API", description = "스터디 그룹 가입 요청 목록을 조회하는 API")
	public ResponseEntity<List<JoinRequest>> getAllJoinRequests(@AuthedUser User user, @PathVariable Long groupId) {
		List<JoinRequest> response = joinRequestService.getAllJoinRequests(user, groupId);

		return ResponseEntity.ok().body(response);
	}

	@PostMapping(value = "/join-request/{requestId}")
	@Operation(summary = "그룹 가입 요청 승인 / 거절", description = "스터디 그룹 가입 요청을 승인 / 거절하는 API")
	public ResponseEntity<Void> updateRequest(
		@AuthedUser User user,
		@PathVariable Long requestId,
		@RequestBody @Valid UpdateJoinRequestStatusRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new JoinRequestException("가입 요청이 올바르지 않습니다.");
		joinRequestService.updateJoinRequest(user, requestId, request);
		return ResponseEntity.ok().build();
	}

}
