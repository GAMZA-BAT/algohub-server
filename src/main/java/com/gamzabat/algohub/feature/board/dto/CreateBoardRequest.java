package com.gamzabat.algohub.feature.board.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBoardRequest(@NotNull Long boardId,
								 @NotNull String title,
								 @NotNull String content) {

}
