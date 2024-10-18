package com.gamzabat.algohub.feature.board.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.board.domain.Board;

import lombok.Builder;

@Builder
public record GetBoardResponse(String author,
							   Long boardId,
							   String boardContent,
							   String boardTitle,
							   String createAt) {

	public static GetBoardResponse toDTO(Board board) {
		return GetBoardResponse.builder()
			.author(board.getAuthor().getNickname())
			.boardId(board.getId())
			.boardTitle(board.getTitle())
			.boardContent(board.getContent())
			.createAt(DateFormatUtil.formatDate(board.getCreatedAt().toLocalDate()))
			.build();

	}
}
