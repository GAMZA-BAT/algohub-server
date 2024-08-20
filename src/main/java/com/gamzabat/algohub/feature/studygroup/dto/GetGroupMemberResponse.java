package com.gamzabat.algohub.feature.studygroup.dto;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class GetGroupMemberResponse {

	private String nickname;
	private LocalDate joinDate;
	private String achivement;
	private Boolean isOwner;
	private String profileImage;
	private Long memberId;

	public GetGroupMemberResponse(String nickname, LocalDate joinDate, String achivement, Boolean isOwner,
		String profileImage, Long memberId) {
		this.nickname = nickname;
		this.joinDate = joinDate;
		this.achivement = achivement;
		this.isOwner = isOwner;
		this.profileImage = profileImage;
		this.memberId = memberId;
	}
}
