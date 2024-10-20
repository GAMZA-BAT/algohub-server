package com.gamzabat.algohub.feature.group.studygroup.dto;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class GetGroupResponse {
	private Long id;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private String introduction;
	private String groupImage;
	private Boolean isOwner;
	private String ownerNickname;

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
