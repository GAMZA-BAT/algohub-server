package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequest(@NotBlank(message = "스터디 이름은 필수 입력 입니다.") String name,
								 @NotNull(message = "스터디 시작 날짜는 필수 입력 입니다.") LocalDate startDate,
								 @NotNull(message = "스터디 종료 날짜는 필수 입력 입니다.") LocalDate endDate,
								 String introduction
) {
}
