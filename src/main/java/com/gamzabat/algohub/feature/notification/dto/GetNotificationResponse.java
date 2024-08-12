package com.gamzabat.algohub.feature.notification.dto;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.notification.domain.Notification;

public record GetNotificationResponse(Long id,
									  String groupName,
									  String groupImage,
									  String message,
									  String subContent,
									  LocalDateTime createdAt,
									  boolean isRead) {
	public static GetNotificationResponse toDTO(Notification notification){
		return new GetNotificationResponse(
			notification.getId(),
			notification.getStudyGroup().getName(),
			notification.getStudyGroup().getGroupImage(),
			notification.getMessage(),
			notification.getSubContent(),
			notification.getCreatedAt(),
			notification.isRead()
		);
	}
}
