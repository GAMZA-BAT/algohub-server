package com.gamzabat.algohub.feature.comment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.user.domain.User;

public interface CommentController<T extends CreateCommentRequest> {
	ResponseEntity<Void> createComment(User user,
		T request, Errors errors);

	ResponseEntity<List<GetCommentResponse>> getCommentList(User user,
		Long baseId);

	ResponseEntity<Void> modifyComment(User user,
		UpdateCommentRequest request, Errors errors);

	ResponseEntity<Void> deleteComment(User user, Long commentId);
}
