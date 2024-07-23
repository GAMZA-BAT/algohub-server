package com.gamzabat.algohub.feature.solution.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateSolutionRequest(@NotNull(message = "아이디 입력은 필수 입력 입니다.") String userName,
									String code,
									@NotNull(message = "문제 번호 입력은 필수 입력 입니다.") Integer problemNumber,
									@NotNull(message = "제출 번호 입력은 필수 입력 입니다.") String submissionId) {
}