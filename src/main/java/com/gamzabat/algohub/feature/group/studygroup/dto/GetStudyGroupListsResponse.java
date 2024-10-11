package com.gamzabat.algohub.feature.group.studygroup.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class GetStudyGroupListsResponse {
	private List<GetStudyGroupResponse> bookmarked;
	private List<GetStudyGroupResponse> done;
	private List<GetStudyGroupResponse> inProgress;
	private List<GetStudyGroupResponse> queued;

	public GetStudyGroupListsResponse(List<GetStudyGroupResponse> bookmarked, List<GetStudyGroupResponse> done,
		List<GetStudyGroupResponse> inProgress,
		List<GetStudyGroupResponse> queued) {
		this.bookmarked = bookmarked;
		this.done = done;
		this.inProgress = inProgress;
		this.queued = queued;
	}
}
