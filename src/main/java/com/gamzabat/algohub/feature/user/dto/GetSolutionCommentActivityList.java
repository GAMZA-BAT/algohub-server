package com.gamzabat.algohub.feature.user.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record GetSolutionCommentActivityList(List<GetSolutionCommentActivity> solutionCommentActivityList) {
}
