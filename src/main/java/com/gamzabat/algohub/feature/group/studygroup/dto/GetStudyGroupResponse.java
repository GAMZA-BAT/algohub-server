package com.gamzabat.algohub.feature.group.studygroup.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.user.domain.User;

public record GetStudyGroupResponse(Long id,
									String name,
									String groupImage,
									String startDate,
									String endDate,
									String introduction,
									String ownerNickname,
									RoleOfGroupMember role,
									boolean isBookmarked,
									boolean isVisible) {
	public static GetStudyGroupResponse toDTO(StudyGroup group, GroupMember member, boolean isBookmarked, User owner,
		boolean isVisible) {
		return new GetStudyGroupResponse(
			group.getId(),
			group.getName(),
			group.getGroupImage(),
			DateFormatUtil.formatDate(group.getStartDate()),
			DateFormatUtil.formatDate(group.getEndDate()),
			group.getIntroduction(),
			owner.getNickname(),
			member.getRole(),
			isBookmarked,
			isVisible
		);
	}
}
