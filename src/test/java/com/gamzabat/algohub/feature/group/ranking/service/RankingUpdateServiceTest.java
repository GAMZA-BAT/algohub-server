package com.gamzabat.algohub.feature.group.ranking.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class RankingUpdateServiceTest {
	@InjectMocks
	private RankingUpdateService rankingUpdateService;
	@Mock
	private RankingRepository rankingRepository;

	private User user, owner, user2, user3, user4;
	private StudyGroup group;
	private GroupMember groupMember1, groupMember2, groupMember3, groupMember4;
	private Ranking ranking1, ranking2, ranking3;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image1").build();
		owner = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image1").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image2").build();
		user3 = User.builder().email("email3").password("password").nickname("nickname3")
			.role(Role.USER).profileImage("image3").build();
		user4 = User.builder().email("email4").password("password").nickname("nickname4")
			.role(Role.USER).profileImage("image4").build();
		group = StudyGroup.builder()
			.name("name")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.groupImage("imageUrl")
			.groupCode("code")
			.build();
		groupMember1 = GroupMember.builder()
			.studyGroup(group)
			.user(user)
			.role(RoleOfGroupMember.OWNER)
			.joinDate(LocalDate.now())
			.build();
		groupMember2 = GroupMember.builder()
			.studyGroup(group)
			.user(user2)
			.role(RoleOfGroupMember.PARTICIPANT)
			.joinDate(LocalDate.now())
			.build();
		groupMember3 = GroupMember.builder()
			.studyGroup(group)
			.user(user3)
			.role(RoleOfGroupMember.ADMIN)
			.joinDate(LocalDate.now())
			.build();
		groupMember4 = GroupMember.builder()
			.studyGroup(group)
			.user(user4)
			.role(RoleOfGroupMember.PARTICIPANT)
			.joinDate(LocalDate.now())
			.build();
		ranking1 = Ranking.builder()
			.member(groupMember1)
			.solvedCount(1)
			.currentRank(1)
			.score(10)
			.rankDiff("-")
			.build();
		ranking2 = Ranking.builder()
			.member(groupMember2)
			.solvedCount(1)
			.currentRank(2)
			.score(8)
			.rankDiff("-")
			.build();
		ranking3 = Ranking.builder()
			.member(groupMember3)
			.solvedCount(1)
			.currentRank(3)
			.score(6)
			.rankDiff("-")
			.build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(owner, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group, 10L);
	}

	@Test
	@DisplayName("랭킹 업데이트 성공")
	void updateRanking() {
		// given
		List<Ranking> rankings = new ArrayList<>();
		rankings.add(ranking1);
		rankings.add(ranking2);
		rankings.add(ranking3);

		ranking3.increaseSolvedCount();
		when(rankingRepository.findAllByStudyGroup(group)).thenReturn(rankings);

		// when
		rankingUpdateService.updateRanking(group);

		// then
		assertThat(ranking3.getCurrentRank()).isEqualTo(1);
		assertThat(ranking3.getRankDiff()).isEqualTo("+2");

		assertThat(ranking1.getCurrentRank()).isEqualTo(2);
		assertThat(ranking1.getRankDiff()).isEqualTo("-1");

		assertThat(ranking2.getCurrentRank()).isEqualTo(3);
		assertThat(ranking2.getRankDiff()).isEqualTo("-1");
	}
}