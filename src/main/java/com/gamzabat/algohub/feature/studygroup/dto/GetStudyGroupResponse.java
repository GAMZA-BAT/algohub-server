package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;

public record GetStudyGroupResponse(Long id,
									String name,
									String groupImage,
									LocalDate startDate,
									LocalDate endDate,
									String introduction,
									String ownerNickname,
									boolean isOwner,
									boolean isBookmarked) {
	public static GetStudyGroupResponse toDTO(StudyGroup group, User user, boolean isBookmarked, User owner) {
		return new GetStudyGroupResponse(
			group.getId(),
			group.getName(),
			group.getGroupImage(),
			group.getStartDate(),
			group.getEndDate(),
			group.getIntroduction(),
			owner.getNickname(),
			owner.getId().equals(user.getId()),
			isBookmarked
		);
	}
}
