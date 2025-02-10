package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.feature.solution.domain.Solution;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class GetSolutionResponse {
	private final Long solutionId;
	private final String problemTitle;
	private final Integer problemLevel;
	private final Integer accuracy;
	private final Integer submitMemberCount;
	private final Integer totalMemberCount;
	private final String nickname;
	private final String profileImage;
	private final String solvedDateTime;
	private final String content;
	private final String result;
	private final Integer memoryUsage;
	private final Integer executionTime;
	private final String language;
	private final Integer codeLength;
	private final Long commentCount;
	private final Boolean isRead;

	public static GetSolutionResponse toDTO(Solution solution, Integer accuracy, Integer submitMemberCount,
		Integer totalMemberCount, Long commentCount, Boolean isRead) {
		return GetSolutionResponse.builder()
			.solutionId(solution.getId())
			.problemTitle(solution.getProblem().getTitle())
			.problemLevel(solution.getProblem().getLevel())
			.accuracy(accuracy)
			.submitMemberCount(submitMemberCount)
			.totalMemberCount(totalMemberCount)
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
			.isRead(isRead)
			.build();
	}

	protected static String convertToCustomResult(String result) {
		if (result.endsWith("점"))
			return BOJResultConstants.CORRECT;
		else if (result.equals(BOJResultConstants.WRONG_OUTPUT_FORMAT))
			return BOJResultConstants.WRONG_OUTPUT_FORMAT_CUSTOM;
		else
			return result.contains("(") ? result.substring(0, result.indexOf("(")).trim() : result;
	}
}