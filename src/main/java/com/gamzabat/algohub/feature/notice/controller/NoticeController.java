package com.gamzabat.algohub.feature.notice.controller;

import java.util.List;

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
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.notice.dto.CreateNoticeRequest;
import com.gamzabat.algohub.feature.notice.dto.GetNoticeResponse;
import com.gamzabat.algohub.feature.notice.dto.UpdateNoticeRequest;
import com.gamzabat.algohub.feature.notice.service.NoticeService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notice")
@Tag(name = "공지 API", description = "공지 관련 API")

public class NoticeController {
	private final NoticeService noticeService;

	@PostMapping
	@Operation(summary = "공지 작성 API")
	public ResponseEntity<Void> createNotice(@AuthedUser User user, @Valid @RequestBody CreateNoticeRequest request,
		Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 공지 생성 요청입니다", errors);
		noticeService.createNotice(user, request);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	@Operation(summary = "공지 하나 조회 API")
	public ResponseEntity<GetNoticeResponse> getNotice(@AuthedUser User user, @RequestParam Long noticeId) {
		GetNoticeResponse response = noticeService.getNotice(user, noticeId);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping(value = "/notice-list")
	@Operation(summary = "공지 목록 조회 API")
	public ResponseEntity<List<GetNoticeResponse>> getNoticeList(@AuthedUser User user,
		@RequestParam Long studyGroupId) {
		List<GetNoticeResponse> response = noticeService.getNoticeList(user, studyGroupId);
		return ResponseEntity.ok().body(response);
	}

	@PatchMapping
	@Operation(summary = "공지 수정 API")
	public ResponseEntity<Void> updateNotice(@AuthedUser User user, @Valid @RequestBody UpdateNoticeRequest request,
		Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 수정 요청입니다", errors);
		noticeService.updateNotice(user, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping
	@Operation(summary = "공지 삭제 API")
	public ResponseEntity<Void> deleteNotice(@AuthedUser User user, @RequestParam Long noticeId) {
		noticeService.deleteNotice(user, noticeId);
		return ResponseEntity.ok().build();
	}
}
