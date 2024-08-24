package com.gamzabat.algohub.feature.board.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBoardRequest(@NotNull(message = "그룹id 는 필수입니다") Long studyGroupId,
								 @NotNull(message = "제목은 필수 입력입니다") String title,
								 @NotNull(message = "본문은 필수 입력입니다") String content
) {

}
