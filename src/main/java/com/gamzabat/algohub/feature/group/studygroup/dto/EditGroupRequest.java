package com.gamzabat.algohub.feature.group.studygroup.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

public record EditGroupRequest(@Size(min = 1, max = 15, message = "스터디 이름은 1글자 이상 15글자 이하로 작성해야 합니다.") String name,
							   LocalDate startDate,
							   LocalDate endDate,
							   String introduction) {
}
