package com.gamzabat.algohub.feature.notification.dto;

import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;

import lombok.Builder;

@Builder
public record GetNotificationSettingResponse(Long groupId,
											 String groupName,
											 boolean allNotifications,
											 boolean newProblem,
											 boolean newSolution,
											 boolean newComment,
											 boolean newMember,
											 boolean deadlineReached) {

	public static GetNotificationSettingResponse toDTO(NotificationSetting setting) {
		return GetNotificationSettingResponse.builder()
			.groupId(setting.getMember().getStudyGroup().getId())
			.groupName(setting.getMember().getStudyGroup().getName())
			.allNotifications(setting.isAllNotifications())
			.newProblem(setting.isNewProblem())
			.newSolution(setting.isNewSolution())
			.newComment(setting.isNewComment())
			.newMember(setting.isNewMember())
			.deadlineReached(setting.isDeadlineReached())
			.build();
	}
}
