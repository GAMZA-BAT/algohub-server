package com.gamzabat.algohub.feature.comment.dto;

import com.gamzabat.algohub.feature.user.domain.User;

import java.time.LocalDateTime;

public record ModifyCommentRequest(Long commentId,
                                   String content
                                   )  {
}
