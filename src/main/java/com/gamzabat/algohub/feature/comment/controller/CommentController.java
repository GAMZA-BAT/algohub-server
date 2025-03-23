package com.gamzabat.algohub.feature.comment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.Operation;

public interface CommentController<T extends CreateCommentRequest> {
	@Operation(summary = "댓글 작성 API")
	ResponseEntity<Void> createComment(User user, Long baseId,
		T request, Errors errors);

	@Operation(summary = "댓글 목록 조회 API", description = "대상 하나에 대한 댓글 전체 조회")
	ResponseEntity<List<GetCommentResponse>> getCommentList(User user,
		Long baseId);

	@Operation(summary = "댓글 수정 API")
	ResponseEntity<Void> modifyComment(User user,
		Long baseId, UpdateCommentRequest request);

	@Operation(summary = "댓글 삭제 API")
	ResponseEntity<Void> deleteComment(User user, Long commentId);
}
