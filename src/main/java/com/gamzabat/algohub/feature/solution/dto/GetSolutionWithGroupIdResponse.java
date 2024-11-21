package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.solution.domain.Solution;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GetSolutionWithGroupIdResponse extends GetSolutionResponse {
	private final Long groupId;

	@Builder
	GetSolutionWithGroupIdResponse(Long solutionId, String problemTitle, Integer problemLevel, String nickname,
		String profileImage, String solvedDateTime, String content, String result, Integer memoryUsage,
		Integer executionTime, String language, Integer codeLength, Long commentCount, Long groupId) {
		super(solutionId, problemTitle, problemLevel, nickname, profileImage, solvedDateTime, content, result,
			memoryUsage, executionTime, language, codeLength, commentCount);
		this.groupId = groupId;
	}

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
