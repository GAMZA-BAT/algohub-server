package com.gamzabat.algohub.feature.group.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;

public record GetStudyGroupWithCodeResponse(Long id,
											String name,
											String groupImage,
											LocalDate startDate,
											LocalDate endDate,
											String introduction,
											String ownerNickname) {

	public static GetStudyGroupWithCodeResponse toDTO(StudyGroup group, User owner) {
		return new GetStudyGroupWithCodeResponse(
			group.getId(),
			group.getName(),
			group.getGroupImage(),
			group.getStartDate(),
			group.getEndDate(),
			group.getIntroduction(),
			owner.getNickname()
		);
	}
}
