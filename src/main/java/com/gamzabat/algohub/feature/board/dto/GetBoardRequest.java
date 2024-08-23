package com.gamzabat.algohub.feature.board.dto;

import jakarta.validation.constraints.NotNull;

public record GetBoardRequest(@NotNull Long BoardId,
							  @NotNull Long StudyGroupId) {
}
