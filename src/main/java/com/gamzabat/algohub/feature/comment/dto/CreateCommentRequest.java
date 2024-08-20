package com.gamzabat.algohub.feature.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateCommentRequest(@NotNull(message = "풀이 고유 아이디는 필수 입력 입니다.") Long solutionId,
								   @NotBlank(message = "내용은 필수 입력 입니다.") String content) {
}
