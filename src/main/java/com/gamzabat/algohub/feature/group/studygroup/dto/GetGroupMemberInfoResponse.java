package com.gamzabat.algohub.feature.group.studygroup.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GetGroupMemberInfoResponse {
	private String email;
	private String nickname;
	private String profileImage;
	private String bjNickname;
	private String description;

	private List<GetStudyGroupToOtherUserResponse> done;
	private List<GetStudyGroupToOtherUserResponse> inProgress;
	private List<GetStudyGroupToOtherUserResponse> queued;
	private List<GetStudyGroupToOtherUserResponse> bookmarked;

}
