package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;

import lombok.Getter;

@Getter
public class GetGroupMemberResponse {

	private final String nickname;
	private final String joinDate;
	private final String achievement;
	private final RoleOfGroupMember role;
	private final String profileImage;
	private final Long memberId;

	public GetGroupMemberResponse(String nickname, LocalDate joinDate, String achievement, RoleOfGroupMember role,
		String profileImage, Long memberId) {
		this.nickname = nickname;
		this.joinDate = DateFormatUtil.formatDate(joinDate);
		this.achievement = achievement;
		this.role = role;
		this.profileImage = profileImage;
		this.memberId = memberId;
	}
}
