package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.constants.BOJResultConstants;
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
			.solvedDateTime(DateFormatUtil.formatDateTime(solution.getSolvedDateTime()))
			.content(solution.getContent())
			.result(convertToCustomResult(solution.getResult()))
			.memoryUsage(solution.getMemoryUsage())
			.executionTime(solution.getExecutionTime())
			.language(solution.getLanguage())
			.codeLength(solution.getCodeLength())
			.commentCount(commentCount)
			.build();
	}

	private static String convertToCustomResult(String result) {
		if (result.endsWith("점"))
			return BOJResultConstants.CORRECT;
		else if (result.equals(BOJResultConstants.WRONG_OUTPUT_FORMAT))
			return BOJResultConstants.WRONG_OUTPUT_FORMAT_CUSTOM;
		else
			return result.contains("(") ? result.substring(0, result.indexOf("(")).trim() : result;
	}
}
