package com.gamzabat.algohub.feature.group.studygroup.dto;

import jakarta.validation.constraints.NotNull;

public record EditGroupVisibilityRequest(@NotNull(message = "그룹 아이디는 필수 입력입니다.") Long groupId,
										 boolean isVisible) {
}
