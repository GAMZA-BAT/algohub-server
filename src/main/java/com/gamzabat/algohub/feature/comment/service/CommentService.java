package com.gamzabat.algohub.feature.comment.service;

import java.util.List;

import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.user.domain.User;

public interface CommentService<T extends CreateCommentRequest> {

	void createComment(User user, Long baseId, T request);

	List<GetCommentResponse> getCommentList(User user, Long baseId);

	void updateComment(User user, Long commentId, UpdateCommentRequest request);

	void deleteComment(User user, Long commentId);
}
