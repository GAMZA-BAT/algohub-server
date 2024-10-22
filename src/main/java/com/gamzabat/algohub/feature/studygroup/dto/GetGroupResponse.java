package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import com.gamzabat.algohub.common.DateFormatUtil;

import lombok.Getter;

@Getter
public class GetGroupResponse {
	private Long id;
	private String name;
	private String startDate;
	private String endDate;
	private String introduction;
	private String groupImage;
	private Boolean isOwner;
	private String ownerNickname;

	public GetGroupResponse(Long id, String name, LocalDate startDate, LocalDate endDate, String introduction,
		String groupImage, Boolean isOwner, String ownerNickname) {
		this.id = id;
		this.name = name;
		this.startDate = DateFormatUtil.formatDate(startDate);
		this.endDate = DateFormatUtil.formatDate(endDate);
		this.introduction = introduction;
		this.groupImage = groupImage;
		this.isOwner = isOwner;
		this.ownerNickname = ownerNickname;
	}
}
