package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;

public record GetStudyGroupWithCodeResponse(Long id,
											String name,
											String groupImage,
											LocalDate startDate,
											LocalDate endDate,
											String introduction) {

	public static GetStudyGroupWithCodeResponse toDTO(StudyGroup group) {
		return new GetStudyGroupWithCodeResponse(
			group.getId(),
			group.getName(),
			group.getGroupImage(),
			group.getStartDate(),
			group.getEndDate(),
			group.getIntroduction()
		);
	}
}
