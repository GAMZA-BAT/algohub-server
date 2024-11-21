package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.solution.domain.Solution;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class GetSolutionWithGroupIdResponse extends GetSolutionResponse {
	private final Long groupId;

	public static GetSolutionWithGroupIdResponse toDTO(Solution solution, Long commentCount) {
		return GetSolutionWithGroupIdResponse.builder()
			.solutionId(solution.getId())
			.problemTitle(solution.getProblem().getTitle())
			.problemLevel(solution.getProblem().getLevel())
			.nickname(solution.getUser().getNickname())
			.profileImage(solution.getUser().getProfileImage())
			.solvedDateTime(DateFormatUtil.formatDateTime(solution.getSolvedDateTime()))
			.content(solution.getContent())
			.result(convertToCustomResult(solution.getResult()))
			.memoryUsage(solution.getMemoryUsage())
			.executionTime(solution.getExecutionTime())
			.language(solution.getLanguage())
			.codeLength(solution.getCodeLength())
			.commentCount(commentCount)
			.groupId(solution.getProblem().getStudyGroup().getId())
			.build();
	}
}
