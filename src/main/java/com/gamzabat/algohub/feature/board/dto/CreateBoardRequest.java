package com.gamzabat.algohub.feature.board.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBoardRequest(@NotNull Long StudyGroupId,
								 @NotNull String title,
								 @NotNull String content
) {

}
