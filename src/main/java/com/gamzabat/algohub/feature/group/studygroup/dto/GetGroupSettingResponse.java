package com.gamzabat.algohub.feature.group.studygroup.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.enums.GroupStatus;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;

public record GetGroupSettingResponse(Long id,
									  String name,
									  String startDate,
									  String endDate,
									  RoleOfGroupMember role,
									  boolean isBookmarked,
									  boolean isVisible,
									  String status) {

	public static GetGroupSettingResponse toDto(StudyGroup group, GroupMember member, boolean isBookmarked,
		boolean isVisible) {
		return new GetGroupSettingResponse(
			group.getId(),
			group.getName(),
			DateFormatUtil.formatDate(group.getStartDate()),
			DateFormatUtil.formatDate(group.getEndDate()),
			member.getRole(),
			isBookmarked,
			isVisible,
			GroupStatus.getStatus(group).getValue()
		);
	}
}
