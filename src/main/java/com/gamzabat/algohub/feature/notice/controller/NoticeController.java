package com.gamzabat.algohub.feature.notice.controller;

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
@RequestMapping("/api")
@Tag(name = "공지 API", description = "공지 관련 API")

public class NoticeController {
	private final String NOTICE_SORT_BY = "createdAt";
	private final NoticeService noticeService;

	@PostMapping("/groups/{groupId}/notices")
	@Operation(summary = "공지 작성 API")
	public ResponseEntity<Void> createNotice(@AuthedUser User user,
		@PathVariable Long groupId,
		@Valid @RequestBody CreateNoticeRequest request,
		Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 공지 생성 요청입니다", errors);
		noticeService.createNotice(user, groupId, request);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/notices/{noticeId}")
	@Operation(summary = "공지 하나 조회 API")
	public ResponseEntity<GetNoticeResponse> getNotice(@AuthedUser User user, @PathVariable Long noticeId) {
		GetNoticeResponse response = noticeService.getNotice(user, noticeId);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping(value = "/groups/{groupId}/notices")
	@Operation(summary = "공지 목록 조회 API")
	public ResponseEntity<Page<GetNoticeResponse>> getNoticeList(@AuthedUser User user,
		@PathVariable Long groupId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(NOTICE_SORT_BY).descending());
		Page<GetNoticeResponse> response = noticeService.getNoticeList(user, groupId, pageable);
		return ResponseEntity.ok().body(response);
	}

	@PatchMapping("/notices/{noticeId}")
	@Operation(summary = "공지 수정 API")
	public ResponseEntity<Void> updateNotice(@AuthedUser User user,
		@PathVariable Long noticeId,
		@Valid @RequestBody UpdateNoticeRequest request,
		Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 수정 요청입니다", errors);
		noticeService.updateNotice(user, noticeId, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/notices/{noticeId}")
	@Operation(summary = "공지 삭제 API")
	public ResponseEntity<Void> deleteNotice(@AuthedUser User user, @PathVariable Long noticeId) {
		noticeService.deleteNotice(user, noticeId);
		return ResponseEntity.ok().build();
	}
}
