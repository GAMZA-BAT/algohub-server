package com.gamzabat.algohub.feature.notice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.comment.controller.CommentController;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.notice.dto.CreateNoticeCommentRequest;
import com.gamzabat.algohub.feature.notice.service.NoticeCommentService;
import com.gamzabat.algohub.feature.user.domain.User;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "공지 댓글 API", description = "공지에 대한 댓글 관련 API")
public class NoticeCommentController implements CommentController<CreateNoticeCommentRequest> {
	private final NoticeCommentService commentService;

	@Override
	@PostMapping("/notices/{noticeId}/comments")
	public ResponseEntity<Void> createComment(@AuthedUser User user,
		@PathVariable Long noticeId,
		@Valid @RequestBody CreateNoticeCommentRequest request, Errors errors) {
		if (errors.hasErrors())
			throw new RequestException("댓글 작성 요청이 올바르지 않습니다.", errors);
		commentService.createComment(user, noticeId, request);
		return ResponseEntity.ok().build();
	}

	@Override
	@GetMapping("/notices/{noticeId}/comments")
	public ResponseEntity<List<GetCommentResponse>> getCommentList(@AuthedUser User user,
		@PathVariable Long noticeId) {
		List<GetCommentResponse> response = commentService.getCommentList(user, noticeId);
		return ResponseEntity.ok().body(response);
	}

	@Override
	@DeleteMapping("/notices/comments/{commentId}")
	public ResponseEntity<Void> deleteComment(@AuthedUser User user, @PathVariable Long commentId) {
		commentService.deleteComment(user, commentId);
		return ResponseEntity.ok().build();
	}

	@Override
	@PatchMapping("/notices/comments/{commentId}")
	public ResponseEntity<Void> modifyComment(@AuthedUser User user,
		@PathVariable Long commentId,
		@Valid @RequestBody UpdateCommentRequest request) {
			commentService.updateComment(user, commentId, request);
		return ResponseEntity.ok().build();
	}

}
