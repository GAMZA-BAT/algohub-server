package com.gamzabat.algohub.feature.recommendation.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

class GroupJoinMonthlyRollingTest {

	@Test
	@DisplayName("GroupJoinMonthlyRolling 엔티티 생성 성공")
	void createGroupJoinMonthlyRolling_Success() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime windowStart = now.minusDays(30);
		LocalDateTime windowEnd = now;

		StudyGroup studyGroup = StudyGroup.builder()
			.name("테스트 스터디 그룹")
			.build();

		// when
		GroupJoinMonthlyRolling joinInfo = GroupJoinMonthlyRolling.builder()
			.studyGroup(studyGroup)
			.windowStart(windowStart)
			.windowEnd(windowEnd)
			.newMembers(5)
			.membersBeforeWindow(20)
			.joinRate(0.25)
			.build();

		// then
		assertThat(joinInfo.getStudyGroup()).isEqualTo(studyGroup);
		assertThat(joinInfo.getWindowStart()).isEqualTo(windowStart);
		assertThat(joinInfo.getWindowEnd()).isEqualTo(windowEnd);
		assertThat(joinInfo.getNewMembers()).isEqualTo(5);
		assertThat(joinInfo.getMembersBeforeWindow()).isEqualTo(20);
		assertThat(joinInfo.getJoinRate()).isEqualTo(0.25);
	}

	@Test
	@DisplayName("가입률 계산 검증")
	void calculateJoinRate_Success() {
		// given
		GroupJoinMonthlyRolling joinInfo = GroupJoinMonthlyRolling.builder()
			.newMembers(5)
			.membersBeforeWindow(20)
			.joinRate(0.25) // 5 / 20 = 0.25
			.build();

		// when & then
		assertThat(joinInfo.getJoinRate()).isEqualTo(0.25);
		assertThat((double)joinInfo.getNewMembers() / joinInfo.getMembersBeforeWindow())
			.isEqualTo(joinInfo.getJoinRate());
	}

	@Test
	@DisplayName("기존 멤버가 0명일 때 가입률 계산")
	void calculateJoinRate_ZeroMembersBefore() {
		// given
		GroupJoinMonthlyRolling joinInfo = GroupJoinMonthlyRolling.builder()
			.newMembers(5)
			.membersBeforeWindow(0)
			.joinRate(0.0) // 5 / 0 = 0.0
			.build();

		// when & then
		assertThat(joinInfo.getJoinRate()).isEqualTo(0.0);
	}
}
