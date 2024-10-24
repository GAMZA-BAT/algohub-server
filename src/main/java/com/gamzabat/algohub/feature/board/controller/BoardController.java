package com.gamzabat.algohub.feature.board.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.dto.GetBoardResponse;
import com.gamzabat.algohub.feature.board.dto.UpdateBoardRequest;
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
	public ResponseEntity<String> createBoard(@AuthedUser User user, @Valid @RequestBody CreateBoardRequest request,
		Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("올바르지 않은 공지 생성 요청입니다", errors);
		boardService.createBoard(user, request);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping
	@Operation(summary = "공지 하나 조회 API")
	public ResponseEntity<GetBoardResponse> getBoard(@AuthedUser User user, @RequestParam Long boardId) {
		GetBoardResponse response = boardService.getBoard(user, boardId);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping(value = "/board-list")
	@Operation(summary = "공지 목록 조회 API")
	public ResponseEntity<List<GetBoardResponse>> getBoardList(@AuthedUser User user, @RequestParam Long studyGroupId) {
		List<GetBoardResponse> response = boardService.getBoardList(user, studyGroupId);
		return ResponseEntity.ok().body(response);
	}

	@PatchMapping
	@Operation(summary = "공지 수정 API")
	public ResponseEntity<String> updateBoard(@AuthedUser User user, @Valid @RequestBody UpdateBoardRequest request) {
		boardService.updateBoard(user, request);
		return ResponseEntity.ok().body("OK");
	}
}
