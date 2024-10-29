package com.gamzabat.algohub.feature.comment.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.comment.domain.Comment;

import lombok.Builder;

@Builder
public record GetCommentResponse(Long commentId,
								 String writerNickname,
								 String writerProfileImage,
								 String content,
								 String createdAt) {
	public static GetCommentResponse toDTO(Comment comment) {
		return GetCommentResponse.builder()
			.commentId(comment.getId())
			.writerNickname(comment.getUser().getNickname())
			.writerProfileImage(comment.getUser().getProfileImage())
			.content(comment.getContent())
			.createdAt(DateFormatUtil.formatDate(comment.getCreatedAt().toLocalDate()))
			.build();
	}
}
