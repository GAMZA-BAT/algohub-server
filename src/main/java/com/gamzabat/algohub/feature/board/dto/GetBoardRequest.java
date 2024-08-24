package com.gamzabat.algohub.feature.board.dto;

import jakarta.validation.constraints.NotNull;

public record GetBoardRequest(@NotNull(message = "공지 id는 필수 입니다") Long boardId,
							  @NotNull(message = "그룹 id는 필수 입니다") Long studyGroupId) {
}
