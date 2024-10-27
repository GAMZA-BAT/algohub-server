package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;

import lombok.Getter;

@Getter
public class GetGroupMemberResponse {

	private final String nickname;
	private final LocalDate joinDate;
	private final String achivement;
	private final RoleOfGroupMember role;
	private final String profileImage;
	private final Long memberId;

	public GetGroupMemberResponse(String nickname, LocalDate joinDate, String achivement, RoleOfGroupMember role,
		String profileImage, Long memberId) {
		this.nickname = nickname;
		this.joinDate = joinDate;
		this.achivement = achivement;
		this.role = role;
		this.profileImage = profileImage;
		this.memberId = memberId;
	}
}
