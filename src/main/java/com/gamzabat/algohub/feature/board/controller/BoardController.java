package com.gamzabat.algohub.feature.board.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.dto.GetBoardRequest;
import com.gamzabat.algohub.feature.board.service.BoardService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")

public class BoardController {
	private final BoardService boardService;

	@PostMapping
	@Operation(summary = "공지 작성API")
	public ResponseEntity<String> createBoard(@AuthedUser User user, @Valid @RequestParam CreateBoardRequest request) {
		boardService.createBoard(user, request);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping
	@Operation(summary = "공지 조회API")
	public ResponseEntity<String> getBoards(@AuthedUser User user, @Valid @RequestParam GetBoardRequest request) {

	}
}
