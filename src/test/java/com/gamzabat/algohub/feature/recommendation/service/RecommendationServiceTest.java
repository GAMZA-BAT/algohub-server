package com.gamzabat.algohub.feature.recommendation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.recommendation.domain.GroupDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.domain.StudyGroupTag;
import com.gamzabat.algohub.feature.recommendation.domain.TagType;
import com.gamzabat.algohub.feature.recommendation.domain.UserDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.dto.HomeRecommendationsResponse;
import com.gamzabat.algohub.feature.recommendation.repository.GroupDifficultyMonthlyRollingRepository;
import com.gamzabat.algohub.feature.recommendation.repository.StudyGroupTagRepository;
import com.gamzabat.algohub.feature.recommendation.repository.UserDifficultyMonthlyRollingRepository;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationServiceTest {

	@Mock
	private StudyGroupTagRepository studyGroupTagRepository;
	@Mock
	private UserDifficultyMonthlyRollingRepository userDifficultyMonthlyRollingRepository;
	@Mock
	private GroupDifficultyMonthlyRollingRepository groupDifficultyMonthlyRollingRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;

	@InjectMocks
	private RecommendationService recommendationService;

	private StudyGroup activeGroup1;
	private StudyGroup activeGroup2;
	private StudyGroup highJoinGroup;
	private StudyGroup similarGroup;

	@BeforeEach
	void setUp() {
		activeGroup1 = createStudyGroup("활발한 스터디 1");
		activeGroup2 = createStudyGroup("활발한 스터디 2");
		highJoinGroup = createStudyGroup("인기 스터디");
		similarGroup = createStudyGroup("비슷한 난이도 스터디");
	}

	@Test
	@DisplayName("이번 주 가장 활발한 스터디 조회 - 성공")
	void getMostActiveThisWeek_Success() {
		// given
		StudyGroupTag highActivityTag = createStudyGroupTag(
			activeGroup1,
			TagType.MOST_ACTIVE_THIS_WEEK,
			0.9,
			LocalDateTime.now().minusDays(1)
		);

		StudyGroupTag mediumActivityTag = createStudyGroupTag(
			activeGroup2,
			TagType.MOST_ACTIVE_THIS_WEEK,
			0.7,  // 중간 활동 점수
			LocalDateTime.now().minusDays(2)
		);

		when(studyGroupTagRepository.findTop1ByTagTypeOrderByScoreDescFirstAchievedAtAsc(TagType.MOST_ACTIVE_THIS_WEEK))
			.thenReturn(Optional.of(highActivityTag));

		// when
		HomeRecommendationsResponse response = recommendationService.getHomeRecommendations(1L);

		// then
		Assertions.assertAll(
			() -> assertThat(response.mostActiveThisWeek()).isNotNull(),
			() -> assertThat(response.mostActiveThisWeek().studyGroup().id()).isEqualTo(activeGroup1.getId()),
			() -> assertThat(response.mostActiveThisWeek().studyGroup().name()).isEqualTo(activeGroup1.getName()),
			() -> assertThat(response.mostActiveThisWeek().score()).isEqualTo(0.9)
		);
	}

	@Test
	@DisplayName("최근 가입률이 높은 스터디 조회 - 성공")
	void getHighJoinRateRecent_Success() {
		// given
		StudyGroupTag highJoinTag = createStudyGroupTag(
			highJoinGroup,
			TagType.HIGH_JOIN_RATE_RECENT,
			0.85,  // 가장 높은 가입률
			LocalDateTime.now().minusDays(1)
		);

		StudyGroupTag mediumJoinTag = createStudyGroupTag(
			createStudyGroup("중간 인기 스터디"),
			TagType.HIGH_JOIN_RATE_RECENT,
			0.65,  // 중간 가입률
			LocalDateTime.now().minusDays(2)
		);

		when(studyGroupTagRepository.findTop1ByTagTypeOrderByScoreDescFirstAchievedAtAsc(TagType.HIGH_JOIN_RATE_RECENT))
			.thenReturn(Optional.of(highJoinTag));

		// when
		HomeRecommendationsResponse response = recommendationService.getHomeRecommendations(1L);

		// then
		Assertions.assertAll(
			() -> assertThat(response.highJoinRateRecent()).isNotNull(),
			() -> assertThat(response.highJoinRateRecent().studyGroup().id()).isEqualTo(highJoinGroup.getId()),
			() -> assertThat(response.highJoinRateRecent().studyGroup().name()).isEqualTo(highJoinGroup.getName()),
			() -> assertThat(response.highJoinRateRecent().score()).isEqualTo(0.85)
		);
	}

	@Test
	@DisplayName("비슷한 난이도 스터디 추천 - 성공")
	void getSimilarDifficulty_Success() {
		// given
		Long userId = 1L;

		User testUser = mock(User.class);
		when(testUser.getId()).thenReturn(userId);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime windowStart = now.minusDays(30);
		LocalDateTime windowEnd = now;

		// 사용자 난이도 설정
		UserDifficultyMonthlyRolling userRolling = createUserDifficultyMonthlyRolling(
			testUser, windowStart, windowEnd, 0.7);

		// 비슷한 난이도의 스터디 그룹 (가장 유사한 그룹)
		GroupDifficultyMonthlyRolling similarGroupRolling = createGroupDifficultyMonthlyRolling(
			similarGroup, windowStart, windowEnd, 0.72);  // 사용자와 가장 가까운 난이도

		// 덜 비슷한 난이도의 스터디 그룹
		GroupDifficultyMonthlyRolling lessSimilarGroupRolling = createGroupDifficultyMonthlyRolling(
			createStudyGroup("덜 비슷한 난이도"), windowStart, windowEnd, 0.85);

		when(userDifficultyMonthlyRollingRepository.findTop1ByUser_IdOrderByWindowEndDesc(userId))
			.thenReturn(Optional.of(userRolling));

		when(groupDifficultyMonthlyRollingRepository.findTopSimilarByWindow(
			eq(windowStart), eq(windowEnd), eq(0.7)))
			.thenReturn(Optional.of(similarGroupRolling));

		// when
		HomeRecommendationsResponse response = recommendationService.getHomeRecommendations(userId);

		// then
		Assertions.assertAll(
			() -> assertThat(response.similarDifficulty()).isNotNull(),
			() -> assertThat(response.similarDifficulty().studyGroup().id()).isEqualTo(similarGroup.getId()),
			() -> assertThat(response.similarDifficulty().studyGroup().name()).isEqualTo(similarGroup.getName()),
			() -> assertThat(response.similarDifficulty().score()).isBetween(0.0, 0.1)  // 0.72 - 0.7 = 0.02 차이
		);
	}

	@Test
	@DisplayName("추천 데이터가 없을 때 기본값 반환")
	void getHomeRecommendations_WhenNoData_ThenReturnDefault() {
		// given
		when(studyGroupTagRepository.findTop1ByTagTypeOrderByScoreDescFirstAchievedAtAsc(any()))
			.thenReturn(Optional.empty());
		when(userDifficultyMonthlyRollingRepository.findTop1ByUser_IdOrderByWindowEndDesc(any()))
			.thenReturn(Optional.empty());
		when(studyGroupRepository.findAll()).thenReturn(List.of());

		// when
		HomeRecommendationsResponse response = recommendationService.getHomeRecommendations(1L);

		// then
		Assertions.assertAll(
			() -> assertThat(response).isNotNull(),
			() -> assertThat(response.mostActiveThisWeek()).isNull(),
			() -> assertThat(response.highJoinRateRecent()).isNull(),
			() -> assertThat(response.similarDifficulty()).isNull()
		);
	}

	// Helper methods
	private StudyGroup createStudyGroup(String name) {
		return StudyGroup.builder()
			.name(name)
			.introduction(name + "소개")
			.groupImage("image_url_" + name + ".jpg")
			.build();
	}

	private StudyGroupTag createStudyGroupTag(StudyGroup group, TagType tagType, double score,
		LocalDateTime firstAchievedAt) {
		return StudyGroupTag.builder()
			.studyGroup(group)
			.tagType(tagType)
			.score(score)
			.firstAchievedAt(firstAchievedAt)
			.windowStart(firstAchievedAt.toLocalDate().withDayOfMonth(1).atStartOfDay())
			.windowEnd(firstAchievedAt.toLocalDate().withDayOfMonth(1).plusMonths(1).atStartOfDay())
			.computedAt(firstAchievedAt)
			.build();
	}

	private UserDifficultyMonthlyRolling createUserDifficultyMonthlyRolling(
		User user, LocalDateTime windowStart, LocalDateTime windowEnd, double avgDifficulty) {
		return UserDifficultyMonthlyRolling.builder()
			.user(user)
			.windowStart(windowStart)
			.windowEnd(windowEnd)
			.avgDifficulty(avgDifficulty)
			.build();
	}

	private GroupDifficultyMonthlyRolling createGroupDifficultyMonthlyRolling(
		StudyGroup group, LocalDateTime windowStart, LocalDateTime windowEnd, double avgDifficulty) {
		return GroupDifficultyMonthlyRolling.builder()
			.studyGroup(group)
			.windowStart(windowStart)
			.windowEnd(windowEnd)
			.avgDifficulty(avgDifficulty)
			.build();
	}
}