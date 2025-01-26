package com.gamzabat.algohub.feature.comment.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCommentRequest(@NotNull(message = "댓글 본문은 필수 입력입니다") String content
) {
}
