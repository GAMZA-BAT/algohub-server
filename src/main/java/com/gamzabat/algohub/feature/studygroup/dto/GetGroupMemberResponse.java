package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;

import lombok.Getter;

@Getter
public class GetGroupMemberResponse {

	private String nickname;
	private String joinDate;
	private String achivement;
	private RoleOfGroupMember role;
	private String profileImage;
	private Long memberId;

	public GetGroupMemberResponse(String nickname, LocalDate joinDate, String achivement, RoleOfGroupMember role,
		String profileImage, Long memberId) {
		this.nickname = nickname;
		this.joinDate = DateFormatUtil.formatDate(joinDate);
		this.achivement = achivement;
		this.role = role;
		this.profileImage = profileImage;
		this.memberId = memberId;
	}
}
