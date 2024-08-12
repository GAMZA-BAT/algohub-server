package com.gamzabat.algohub.feature.solution.dto;

import java.time.format.DateTimeFormatter;

import com.gamzabat.algohub.feature.solution.domain.Solution;

import lombok.Builder;

@Builder
public record GetSolutionResponse(Long solutionId,
								  String nickname,
								  String profileImage,
								  String solvedDateTime,
								  String content,
								  boolean isCorrect,
								  Integer memoryUsage,
								  Integer executionTime,
								  String language,
								  Integer codeLength,
								  Long commentCount) {
	public static GetSolutionResponse toDTO(Solution solution, Long commentCount){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String solvedDateTime = solution.getSolvedDateTime().format(formatter);

		return GetSolutionResponse.builder()
			.solutionId(solution.getId())
			.nickname(solution.getUser().getNickname())
			.profileImage(solution.getUser().getProfileImage())
			.solvedDateTime(solvedDateTime)
			.content(solution.getContent())
			.isCorrect(solution.isCorrect())
			.memoryUsage(solution.getMemoryUsage())
			.executionTime(solution.getExecutionTime())
			.language(solution.getLanguage())
			.codeLength(solution.getCodeLength())
			.commentCount(commentCount)
			.build();
	}
}
