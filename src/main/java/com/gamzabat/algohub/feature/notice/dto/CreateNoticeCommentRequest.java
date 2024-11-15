package com.gamzabat.algohub.feature.notice.dto;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateNoticeCommentRequest(@NotBlank(message = "내용은 필수 입력 입니다.") String content) implements
	CreateCommentRequest {
}
