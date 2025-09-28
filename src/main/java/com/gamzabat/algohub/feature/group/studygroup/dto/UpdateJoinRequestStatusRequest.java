package com.gamzabat.algohub.feature.group.studygroup.dto;

import com.gamzabat.algohub.enums.JoinRequestStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateJoinRequestStatusRequest(@NotNull(message = "status 는 필수입니다.") JoinRequestStatus status) {
}
