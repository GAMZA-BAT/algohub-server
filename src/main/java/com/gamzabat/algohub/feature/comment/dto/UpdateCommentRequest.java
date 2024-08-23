package com.gamzabat.algohub.feature.comment.dto;

public record UpdateCommentRequest(Long commentId,
								   String content
) {
}
