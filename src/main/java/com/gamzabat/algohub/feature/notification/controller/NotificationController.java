package com.gamzabat.algohub.feature.notification.controller;

import static org.springframework.http.MediaType.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.notification.dto.EditNotificationSettingRequest;
import com.gamzabat.algohub.feature.notification.dto.GetNotificationResponse;
import com.gamzabat.algohub.feature.notification.dto.GetNotificationSettingResponse;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.notification.service.NotificationSettingService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@Tag(name = "알림 API", description = "알림 관련 API")
public class NotificationController {
	private final NotificationService notificationService;
	private final NotificationSettingService notificationSettingService;

	@GetMapping(value = "/subscribe", produces = TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "SSE 알림 연결 API")
	public SseEmitter streamNotifications(@AuthedUser User user,
		@RequestHeader(value = "Last-Event_Id", required = false, defaultValue = "") String lastEventId) {
		return notificationService.subscribe(user, lastEventId);
	}

	@GetMapping
	@Operation(summary = "알림 목록 조회 API")
	public ResponseEntity<List<GetNotificationResponse>> getNotifications(@AuthedUser User user) {
		return ResponseEntity.ok().body(notificationService.getNotifications(user));
	}

	@PatchMapping
	@Operation(summary = "알림 읽음을 표시하는 API", description = "알림 탭을 연 후 닫을 때 호출하면 봤던 알림들은 읽음 처리 되는 API")
	public void updateIsRead(@AuthedUser User user) {
		notificationService.updateIsRead(user);
	}

	@GetMapping(value = "/setting")
	@Operation(summary = "알림 설정 목록 조회 API", description = "유저가 가입한 그룹들에 대해 알림 설정 목록을 조회하는 API")
	public ResponseEntity<List<GetNotificationSettingResponse>> getNotificationSettings(@AuthedUser User user) {
		return ResponseEntity.ok().body(notificationSettingService.getNotificationSettings(user));
	}

	@PatchMapping(value = "/setting")
	@Operation(summary = "알림 설정 수정 API")
	public ResponseEntity<Void> updateNotificationSettings(@AuthedUser User user, @Valid @RequestBody
	EditNotificationSettingRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("알림 설정 수정 요청이 올바르지 않습니다.", errors);
		notificationSettingService.editNotificationSettings(user, request);
		return ResponseEntity.ok().build();
	}

}
