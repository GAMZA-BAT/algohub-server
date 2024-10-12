package com.gamzabat.algohub.feature.group.ranking.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.group.ranking.exception.CannotFoundRankingException;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
	@InjectMocks
	private RankingService rankingService;
	@Mock
	private RankingRepository rankingRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;

	private User user, owner, user2, user3, user4;
	private StudyGroup group;
	private Problem problem1, problem2;
	private GroupMember groupMember1, groupMember2, groupMember3, groupMember4;
	private Ranking ranking1, ranking2, ranking3, ranking4;
	private final Long groupId = 10L;

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
			.rankDiff("+1")
			.build();
		ranking3 = Ranking.builder()
			.member(groupMember3)
			.solvedCount(1)
			.currentRank(3)
			.rankDiff("-1")
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
	@DisplayName("전체랭킹 조회 성공")
	void getAllRank_SuccessByOwner() {
		//given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		List<Ranking> ranking = new ArrayList<>();
		ranking.add(ranking2);
		ranking.add(ranking3);
		ranking.add(ranking1);
		ranking.add(ranking4);
		when(rankingRepository.findAllByStudyGroup(group)).thenReturn(ranking);

		//when
		List<GetRankingResponse> result = rankingService.getAllRank(user2, 10L);

		//then
		assertThat(result.size()).isEqualTo(4);

		for (int i = 0; i < 4; i++) {
			assertThat(result.get(i).getProfileImage()).isEqualTo("image" + (i + 1));
			assertThat(result.get(i).getSolvedCount()).isEqualTo(3 - i);
			assertThat(result.get(i).getUserNickname()).isEqualTo("nickname" + (i + 1));
			assertThat(result.get(i).getRank()).isEqualTo(i + 1);
		}
	}

	@Test
	@DisplayName("전체랭킹 조회 실패 : 그룹을 못 찾은 경우")
	void getAllRank_FailedByCannotFoundGroup() {
		//given
		when(studyGroupRepository.findById(9L)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> rankingService.getAllRank(user, 9L))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "그룹을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("전체랭킹 조회 실패 : 랭킹을 확인할 권한이 없는 경우")
	void getAllRank_FailedByAccess() {
		//given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);

		//then
		assertThatThrownBy(() -> rankingService.getAllRank(user2, groupId))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "랭킹을 확인할 권한이 없습니다.");
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 성공")
	void getTopRanking() {
		//given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		List<Ranking> ranking = new ArrayList<>();
		ranking.add(ranking2);
		ranking.add(ranking3);
		ranking.add(ranking4);
		ranking.add(ranking1);
		when(rankingRepository.findAllByStudyGroup(group)).thenReturn(ranking);

		//when
		List<GetRankingResponse> result = rankingService.getTopRank(user2, 10L);

		//then
		assertThat(result.size()).isEqualTo(3);

		for (int i = 0; i < 3; i++) {
			assertThat(result.get(i).getProfileImage()).isEqualTo("image" + (i + 1));
			assertThat(result.get(i).getSolvedCount()).isEqualTo(3 - i);
			assertThat(result.get(i).getUserNickname()).isEqualTo("nickname" + (i + 1));
			assertThat(result.get(i).getRank()).isEqualTo(i + 1);
		}
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 성공 : 모두 0 solved인 경우")
	void getTopRanking_EmptyRank() {
		//given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		List<Ranking> ranking = new ArrayList<>();
		ranking.add(Ranking.builder()
			.member(groupMember1)
			.solvedCount(0)
			.build());
		ranking.add(Ranking.builder()
			.member(groupMember2)
			.solvedCount(0)
			.build());
		ranking.add(Ranking.builder()
			.member(groupMember3)
			.solvedCount(0)
			.build());
		ranking.add(Ranking.builder()
			.member(groupMember4)
			.solvedCount(0)
			.build());
		when(rankingRepository.findAllByStudyGroup(group)).thenReturn(ranking);

		//when
		List<GetRankingResponse> result = rankingService.getTopRank(user2, 10L);

		//then
		assertThat(result.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 실패 : 그룹을 못 찾은 경우")
	void getTopRanking_FailedByCannotFoundGroup() {
		//given
		when(studyGroupRepository.findById(9L)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> rankingService.getTopRank(user, 9L))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "그룹을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 실패 : 랭킹을 확인할 권한이 없는 경우")
	void getTopRanking_FailedByAccess() {
		//given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);

		//then
		assertThatThrownBy(() -> rankingService.getTopRank(user2, groupId))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "랭킹을 확인할 권한이 없습니다.");
	}

	@Test
	@DisplayName("랭킹 업데이트 성공")
	void updateRanking() {
		// given
		List<Ranking> rankings = new ArrayList<>();
		rankings.add(ranking1);
		rankings.add(ranking2);
		rankings.add(ranking3);
		when(rankingRepository.findAllByStudyGroup(group)).thenReturn(rankings);
		when(rankingRepository.findByMember(groupMember3)).thenReturn(Optional.of(ranking3));

		// when - user3의 랭킹이 +2
		rankingService.updateRanking(groupMember3);
		rankingService.updateRanking(groupMember3);
		rankingService.updateRanking(groupMember3);

		// then
		assertThat(ranking3.getMember()).isEqualTo(groupMember3);
		assertThat(ranking3.getCurrentRank()).isEqualTo(1);
		assertThat(ranking3.getSolvedCount()).isEqualTo(4);
		assertThat(ranking3.getRankDiff()).isEqualTo("+1");

		assertThat(ranking1.getMember()).isEqualTo(groupMember1);
		assertThat(ranking1.getCurrentRank()).isEqualTo(2);
		assertThat(ranking1.getSolvedCount()).isEqualTo(3);
		assertThat(ranking1.getRankDiff()).isEqualTo("-1");

		assertThat(ranking2.getMember()).isEqualTo(groupMember2);
		assertThat(ranking2.getCurrentRank()).isEqualTo(3);
		assertThat(ranking2.getSolvedCount()).isEqualTo(2);
		assertThat(ranking2.getRankDiff()).isEqualTo("-");
	}

	@Test
	@DisplayName("랭킹 업데이트 실패 : 유저 랭킹 정보 조회 실패")
	void updateRankingFailed_1() {
		// given
		when(rankingRepository.findByMember(groupMember3)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> rankingService.updateRanking(groupMember3))
			.isInstanceOf(CannotFoundRankingException.class)
			.hasFieldOrPropertyWithValue("error", "유저의 랭킹 정보를 조회할 수 없습니다.");
	}
}