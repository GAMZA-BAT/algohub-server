package com.gamzabat.algohub.feature.studygroup.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateGroupRequest(@Size(min = 1, max = 15, message = "스터디 이름은 1글자 이상 15글자 이하로 작성해야 합니다.") String name,
                                 @NotNull(message = "스터디 시작 날짜는 필수 입력 입니다.") LocalDate startDate,
                                 @NotNull(message = "스터디 종료 날짜는 필수 입력 입니다.") LocalDate endDate,
                                 String introduction
) {
}
