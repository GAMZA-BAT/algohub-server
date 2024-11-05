package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateSolutionCommentRequest(@NotNull(message = "풀이 고유 아이디는 필수 입력 입니다.") Long solutionId,
										   @NotBlank(message = "내용은 필수 입력 입니다.") String content) implements
	CreateCommentRequest {
}
