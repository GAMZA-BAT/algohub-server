package com.gamzabat.algohub.feature.board.dto;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.board.domain.Board;

import lombok.Builder;

@Builder
public record GetBoardResponse(String author,
							   Long boardId,
							   String boardContent,
							   String boardTitle,
							   LocalDateTime createAt) {

	public static GetBoardResponse toDTO(Board board) {
		return GetBoardResponse.builder()
			.author(board.getAuthor().getNickname())
			.boardId(board.getId())
			.boardTitle(board.getTitle())
			.boardContent(board.getContent())
			.createAt(board.getCreatedAt())
			.build();

	}
}
