package com.gamzabat.algohub.feature.board.dto;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateBoardCommentRequest(@NotNull(message = "공지사항 아이디는 필수 입력 입니다.") Long boardId,
										@NotBlank(message = "내용은 필수 입력 입니다.") String content) implements
	CreateCommentRequest {
}
