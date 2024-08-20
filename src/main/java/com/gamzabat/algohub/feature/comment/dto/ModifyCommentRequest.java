package com.gamzabat.algohub.feature.comment.dto;

public record ModifyCommentRequest(Long commentId,
								   String content
) {
}
