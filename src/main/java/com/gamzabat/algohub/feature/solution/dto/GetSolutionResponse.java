package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.solution.domain.Solution;

import lombok.Builder;

@Builder
public record GetSolutionResponse(Long solutionId,
								  String nickname,
								  String profileImage,
								  String solvedDateTime,
								  String content,
								  String result,
								  Integer memoryUsage,
								  Integer executionTime,
								  String language,
								  Integer codeLength,
								  Long commentCount) {
	public static GetSolutionResponse toDTO(Solution solution, Long commentCount) {
		return GetSolutionResponse.builder()
			.solutionId(solution.getId())
			.nickname(solution.getUser().getNickname())
			.profileImage(solution.getUser().getProfileImage())
			.solvedDateTime(DateFormatUtil.formatDate(solution.getSolvedDateTime().toLocalDate()))
			.content(solution.getContent())
			.result(solution.getResult())
			.memoryUsage(solution.getMemoryUsage())
			.executionTime(solution.getExecutionTime())
			.language(solution.getLanguage())
			.codeLength(solution.getCodeLength())
			.commentCount(commentCount)
			.build();
	}
}
