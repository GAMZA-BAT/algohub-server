package com.gamzabat.algohub.feature.notification.dto;

import jakarta.validation.constraints.NotNull;

public record EditNotificationSettingRequest(@NotNull(message = "그룹 아이디는 필수 입력입니다.") Long groupId,
											 boolean allNotifications,
											 boolean newProblem,
											 boolean newSolution,
											 boolean newComment,
											 boolean newMember,
											 boolean deadlineReached) {
}
