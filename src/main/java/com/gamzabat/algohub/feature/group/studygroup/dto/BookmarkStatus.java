package com.gamzabat.algohub.feature.group.studygroup.dto;

import lombok.Getter;

@Getter
public enum BookmarkStatus {
	BOOKMARKED("BOOKMARKED"),
	UNMARKED("UNMARKED");

	private final String description;

	BookmarkStatus(String description) {
		this.description = description;
	}

}
