package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class GetGroupResponse {
	private final Long id;
	private final String name;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final String introduction;
	private final String groupImage;
	private final Boolean isOwner;
	private final String ownerNickname;

	public GetGroupResponse(Long id, String name, LocalDate startDate, LocalDate endDate, String introduction,
		String groupImage, Boolean isOwner, String ownerNickname) {
		this.id = id;
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.introduction = introduction;
		this.groupImage = groupImage;
		this.isOwner = isOwner;
		this.ownerNickname = ownerNickname;
	}
}
