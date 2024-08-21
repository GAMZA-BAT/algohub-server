package com.gamzabat.algohub.feature.board.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")

public class BoardController {
	private final BoardController boardController;

	@PostMapping
	@Operation(summary = "게시판 작성API")
	public ResponseEntity<Object>
}
