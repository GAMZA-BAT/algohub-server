package com.gamzabat.algohub.feature.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateBoardRequest(@NotNull(message = "게시글 id는 필수 입니다.") Long boardId,
								 @NotBlank(message = "제목은 필수 입력입니다") String title,
								 @NotBlank(message = "본문은 필수 입력입니다") String content) {
}
