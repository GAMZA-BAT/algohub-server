package com.gamzabat.algohub.feature.board.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.service.BoardService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
@Tag(name = "게시판 API", description = "게시판 관련 API")

public class BoardController {
	private final BoardService boardService;

	@PostMapping
	@Operation(summary = "공지 작성API")
	public ResponseEntity<String> createBoard(@AuthedUser User user, @Valid @RequestBody CreateBoardRequest request) {
		boardService.createBoard(user, request);
		return ResponseEntity.ok().body("OK");
	}
}
