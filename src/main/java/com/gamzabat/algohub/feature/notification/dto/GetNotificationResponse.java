package com.gamzabat.algohub.feature.notification.dto;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.notification.domain.Notification;

import jakarta.annotation.Nullable;

public record GetNotificationResponse(Long id,
									  Long groupId,
									  @Nullable Long problemId,
									  @Nullable Long solutionId,
									  String groupName,
									  String groupImage,
									  String message,
									  String subContent,
									  LocalDateTime createdAt,
									  boolean isRead) {
	public static GetNotificationResponse toDTO(Notification notification) {
		return new GetNotificationResponse(
			notification.getId(),
			notification.getStudyGroup().getId(),
			notification.getProblem() != null ? notification.getProblem().getId() : null,
			notification.getSolution() != null ? notification.getSolution().getId() : null,
			notification.getStudyGroup().getName(),
			notification.getStudyGroup().getGroupImage(),
			notification.getMessage(),
			notification.getSubContent(),
			notification.getCreatedAt(),
			notification.isRead()
		);
	}
}
