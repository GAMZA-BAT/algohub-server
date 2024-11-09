package com.gamzabat.algohub.feature.group.studygroup.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.user.domain.User;

public record GetStudyGroupToOtherUserResponse(Long id,
											   String name,
											   String groupImage,
											   String startDate,
											   String endDate,
											   String introduction,
											   String OwnerNickname,
											   RoleOfGroupMember roleOfGroupMember) {
	public static GetStudyGroupToOtherUserResponse toDTO(StudyGroup group, GroupMember groupMember, User owner) {
		return new GetStudyGroupToOtherUserResponse(
			group.getId(),
			group.getName(),
			group.getGroupImage(),
			DateFormatUtil.formatDate(group.getStartDate()),
			DateFormatUtil.formatDate(group.getEndDate()),
			group.getIntroduction(),
			owner.getNickname(),
			groupMember.getRole()
		);
	}
}