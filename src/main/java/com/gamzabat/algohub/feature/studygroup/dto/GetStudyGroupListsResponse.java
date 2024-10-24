package com.gamzabat.algohub.feature.studygroup.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class GetStudyGroupListsResponse {
	private final List<GetStudyGroupResponse> bookmarked;
	private final List<GetStudyGroupResponse> done;
	private final List<GetStudyGroupResponse> inProgress;
	private final List<GetStudyGroupResponse> queued;

	public GetStudyGroupListsResponse(List<GetStudyGroupResponse> bookmarked, List<GetStudyGroupResponse> done,
		List<GetStudyGroupResponse> inProgress,
		List<GetStudyGroupResponse> queued) {
		this.bookmarked = bookmarked;
		this.done = done;
		this.inProgress = inProgress;
		this.queued = queued;
	}
}
