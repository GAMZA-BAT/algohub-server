package com.gamzabat.algohub.enums;

import java.time.LocalDate;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupStatus {
	QUEUED("Queued"),
	IN_PROGRESS("InProgress"),
	DONE("Done");

	private final String value;

	public static GroupStatus getStatus(StudyGroup group) {
		LocalDate today = LocalDate.now();
		if (group.getStartDate().isAfter(today))
			return QUEUED;
		if (group.getEndDate().isBefore(today))
			return DONE;

		return IN_PROGRESS;
	}
}
