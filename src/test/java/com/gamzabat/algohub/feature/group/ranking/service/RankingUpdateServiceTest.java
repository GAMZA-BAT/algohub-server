package com.gamzabat.algohub.feature.group.ranking.service;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.group.studygroup.service.StudyGroupService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@SpringBootTest
public class RankingUpdateServiceTest {
	@MockBean
	private RankingRepository rankingRepository;
	@MockBean
	private StudyGroupRepository studyGroupRepository;
	@MockBean
	private GroupMemberRepository groupMemberRepository;
	@Autowired
	private StudyGroupService studyGroupService;
	@MockBean
	private RankingUpdateService rankingUpdateService;

	private User user, owner, user2, user3, user4;
	private StudyGroup group;
	private Problem problem1, problem2;
	private GroupMember groupMember1, groupMember2, groupMember3, groupMember4;
	private Ranking ranking1, ranking2, ranking3, ranking4;
	private final Long groupId = 10L;
	@MockBean
	private UserRepository userRepository;

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

		problem1 = Problem.builder()
			.studyGroup(group)
			.build();
		problem2 = Problem.builder()
			.studyGroup(group)
			.build();
		ranking1 = Ranking.builder()
			.member(groupMember1)
			.solvedCount(3)
			.currentRank(1)
			.rankDiff("-")
			.build();
		ranking2 = Ranking.builder()
			.member(groupMember2)
			.solvedCount(2)
			.currentRank(2)
			.rankDiff("-")
			.build();
		ranking3 = Ranking.builder()
			.member(groupMember3)
			.solvedCount(1)
			.currentRank(3)
			.rankDiff("-")
			.build();
		ranking4 = Ranking.builder()
			.member(groupMember4)
			.solvedCount(0)
			.currentRank(4)
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
	@DisplayName("deleteMember 후 AOP 랭킹 업데이트 호출 성공")
	void testAopTriggerAfterDeleteMemberFromStudyGroup() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.of(groupMember1));
		when(userRepository.findById(user2.getId())).thenReturn(Optional.ofNullable(user2));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(groupMember2));
		doNothing().when(rankingUpdateService).updateRanking(group);
		// when
		studyGroupService.deleteMember(user, user2.getId(), groupId);
		// then
		verify(rankingUpdateService, times(1)).updateRanking(group);
	}

	@Test
	@DisplayName("deleteGroup 후 AOP 랭킹 업데이트 호출 성공")
	void testAOPTriggerAfterDeleteGroup() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(groupMember2));
		doNothing().when(rankingUpdateService).updateRanking(group);
		// when
		studyGroupService.deleteGroup(user2, groupId);
		// then
		verify(rankingUpdateService, times(1)).updateRanking(group);
	}

	// @Test
	// @DisplayName("랭킹 업데이트 성공")
	// void updateRanking() {
	// 	// given
	// 	List<Ranking> rankings = new ArrayList<>();
	// 	rankings.add(ranking1);
	// 	rankings.add(ranking2);
	// 	rankings.add(ranking3);
	// 	rankings.add(ranking4);
	//
	// 	ranking3.increaseSolvedCount();
	// 	ranking3.increaseSolvedCount();
	// 	ranking3.increaseSolvedCount();
	// 	when(rankingRepository.findAllByStudyGroup(group)).thenReturn(rankings);
	//
	// 	// when - user3의 랭킹이 +2
	// 	rankingUpdateService.updateRanking(group);
	// 	System.out.println(ranking3.getSolvedCount());
	// 	for (Ranking ranking : rankings) {
	// 		System.out.println("-------ranking. user : " + ranking.getMember().getUser().getNickname());
	// 		System.out.println("ranking.getCurrentRank() = " + ranking.getCurrentRank());
	// 		System.out.println("ranking.getRankDiff() = " + ranking.getRankDiff());
	// 		System.out.println("ranking.getSolvedCount() = " + ranking.getSolvedCount());
	// 	}
	// 	// then
	// 	assertThat(ranking3.getMember()).isEqualTo(groupMember3);
	// 	assertThat(ranking3.getCurrentRank()).isEqualTo(1);
	// 	assertThat(ranking3.getSolvedCount()).isEqualTo(4);
	// 	assertThat(ranking3.getRankDiff()).isEqualTo("+2");
	//
	// 	assertThat(ranking1.getMember()).isEqualTo(groupMember1);
	// 	assertThat(ranking1.getCurrentRank()).isEqualTo(2);
	// 	assertThat(ranking1.getSolvedCount()).isEqualTo(3);
	// 	assertThat(ranking1.getRankDiff()).isEqualTo("-1");
	//
	// 	assertThat(ranking2.getMember()).isEqualTo(groupMember2);
	// 	assertThat(ranking2.getCurrentRank()).isEqualTo(3);
	// 	assertThat(ranking2.getSolvedCount()).isEqualTo(2);
	// 	assertThat(ranking2.getRankDiff()).isEqualTo("-1");
	// }
}
