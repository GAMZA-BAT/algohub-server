package com.gamzabat.algohub.feature.group.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;

import lombok.Getter;

@Getter
public class GetGroupResponse {
	private final Long id;
	private final String name;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final String introduction;
	private final String groupImage;
	private final RoleOfGroupMember role;
	private final String ownerNickname;

	public GetGroupResponse(Long id, String name, LocalDate startDate, LocalDate endDate, String introduction,
		String groupImage, RoleOfGroupMember role, String ownerNickname) {
		this.id = id;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.introduction = introduction;
		this.groupImage = groupImage;
		this.role = role;
		this.ownerNickname = ownerNickname;
	}
}
