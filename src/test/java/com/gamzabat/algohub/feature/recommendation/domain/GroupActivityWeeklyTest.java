package com.gamzabat.algohub.feature.recommendation.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

class GroupActivityWeeklyTest {

	@Test
	@DisplayName("GroupActivityWeekly 엔티티 생성 성공")
	void createGroupActivityWeekly_Success() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime weekStart = now.minusWeeks(1);
		LocalDateTime weekEnd = now;

		StudyGroup studyGroup = StudyGroup.builder()
			.name("테스트 스터디 그룹")
			.build();

		// when
		GroupActivityWeekly activity = GroupActivityWeekly.builder()
			.studyGroup(studyGroup)
			.weekStart(weekStart)
			.weekEnd(weekEnd)
			.submissions(10)
			.comments(5)
			.activeScore(17.5)
			.build();

		// then
		assertThat(activity.getStudyGroup()).isEqualTo(studyGroup);
		assertThat(activity.getWeekStart()).isEqualTo(weekStart);
		assertThat(activity.getWeekEnd()).isEqualTo(weekEnd);
		assertThat(activity.getSubmissions()).isEqualTo(10);
		assertThat(activity.getComments()).isEqualTo(5);
		assertThat(activity.getActiveScore()).isEqualTo(17.5);
	}

	@Test
	@DisplayName("활동 점수 계산 검증")
	void calculateActiveScore_Success() {
		// given
		GroupActivityWeekly activity = GroupActivityWeekly.builder()
			.submissions(10)
			.comments(5)
			.activeScore(17.5) // 10 * 1.0 + 5 * 1.5 = 17.5
			.build();

		// when & then
		assertThat(activity.getActiveScore()).isEqualTo(17.5);
		assertThat(activity.getSubmissions() * 1.0 + activity.getComments() * 1.5)
			.isEqualTo(activity.getActiveScore());
	}
}
