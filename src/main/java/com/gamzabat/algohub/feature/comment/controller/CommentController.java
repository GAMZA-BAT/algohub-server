package com.gamzabat.algohub.feature.comment.controller;

import java.util.List;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.ModifyCommentRequest;
import com.gamzabat.algohub.feature.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.exception.RequestException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
@Tag(name = "댓글 API", description = "풀이에 대한 댓글 관련 API")
public class CommentController {
	private final CommentService commentService;

	@PostMapping
	@Operation(summary = "댓글 작성 API")
	public ResponseEntity<Object> createComment(@AuthedUser User user,
												@Valid @RequestBody CreateCommentRequest request, Errors errors){
		if(errors.hasErrors())
			throw new RequestException("댓글 작성 요청이 올바르지 않습니다.",errors);
		commentService.createComment(user,request);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping
	@Operation(summary = "댓글 목록 조회 API", description = "풀이 하나에 대한 댓글 전체 조회")
	public ResponseEntity<List<GetCommentResponse>> getCommentList(@AuthedUser User user, @RequestParam Long solutionId){
		List<GetCommentResponse> response = commentService.getCommentList(user,solutionId);
		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping
	@Operation(summary = "댓글 삭제 API")
	public ResponseEntity<Object> deleteComment(@AuthedUser User user, @RequestParam Long commentId){
		commentService.deleteComment(user,commentId);
		return ResponseEntity.ok().body("OK");
	}
	@PostMapping("/modify")
	@Operation(summary = "댓글 수정 API")
	public ResponseEntity<Object> modifyComment(@Valid @RequestBody ModifyCommentRequest request, Errors errors){
		if(errors.hasErrors())
			throw new RequestException("수정 요청이 올바르지 않습니다",errors);
		commentService.modifyComment(request);
		return ResponseEntity.ok().body("OK");
	}
}
