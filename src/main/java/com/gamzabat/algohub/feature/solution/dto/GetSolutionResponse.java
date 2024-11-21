package com.gamzabat.algohub.feature.solution.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.feature.solution.domain.Solution;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class GetSolutionResponse {
	private Long solutionId;
	private String problemTitle;
	private Integer problemLevel;
	private String nickname;
	private String profileImage;
	private String solvedDateTime;
	private String content;
	private String result;
	private Integer memoryUsage;
	private Integer executionTime;
	private String language;
	private Integer codeLength;
	private Long commentCount;

	public static GetSolutionResponse toDTO(Solution solution, Long commentCount) {
		return GetSolutionResponse.builder()
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