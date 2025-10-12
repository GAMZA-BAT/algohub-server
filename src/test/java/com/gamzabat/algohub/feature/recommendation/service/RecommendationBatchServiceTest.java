package com.gamzabat.algohub.feature.recommendation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.recommendation.domain.GroupActivityWeekly;
import com.gamzabat.algohub.feature.recommendation.domain.GroupDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.domain.GroupJoinMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.domain.StudyGroupTag;
import com.gamzabat.algohub.feature.recommendation.domain.UserDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.repository.GroupActivityWeeklyRepository;
import com.gamzabat.algohub.feature.recommendation.repository.GroupDifficultyMonthlyRollingRepository;
import com.gamzabat.algohub.feature.recommendation.repository.GroupJoinMonthlyRollingRepository;
import com.gamzabat.algohub.feature.recommendation.repository.StudyGroupTagRepository;
import com.gamzabat.algohub.feature.recommendation.repository.UserDifficultyMonthlyRollingRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionCommentRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationBatchServiceTest {

	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private SolutionRepository solutionRepository;
	@Mock
	private SolutionCommentRepository solutionCommentRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private GroupActivityWeeklyRepository groupActivityWeeklyRepository;
	@Mock
	private GroupJoinMonthlyRollingRepository groupJoinMonthlyRollingRepository;
	@Mock
	private GroupDifficultyMonthlyRollingRepository groupDifficultyMonthlyRollingRepository;
	@Mock
	private UserDifficultyMonthlyRollingRepository userDifficultyMonthlyRollingRepository;
	@Mock
	private StudyGroupTagRepository studyGroupTagRepository;

	@InjectMocks
	private RecommendationBatchService recommendationBatchService;

	private StudyGroup testGroup;
	private User testUser;
	private Problem testProblem;

	@BeforeEach
	void setUp() {
		testGroup = StudyGroup.builder()
			.name("테스트 스터디 그룹")
			.build();

		testUser = User.builder()
			.email("test@example.com")
			.password("password")
			.nickname("테스트유저")
			.role(com.gamzabat.algohub.enums.Role.USER)
			.build();

		testProblem = Problem.builder()
			.title("테스트 문제")
			.level(3)
			.number(1)
			.studyGroup(testGroup)
			.build();
	}

	@Test
	@DisplayName("이번주 가장 많이 활동한 스터디 계산 및 저장 성공")
	void calculateAndSaveMostActiveStudy_Success() {
		// given
		List<StudyGroup> groups = List.of(testGroup);
		LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
		LocalDateTime weekEnd = weekStart.plusWeeks(1).minusNanos(1);

		when(studyGroupRepository.findAll()).thenReturn(groups);
		when(solutionRepository.countByStudyGroupAndSolvedDateTimeBetween(eq(testGroup), any(), any()))
			.thenReturn(10L);
		when(solutionCommentRepository.countByStudyGroupAndCreatedAtBetween(eq(testGroup), any(), any()))
			.thenReturn(5L);

		GroupActivityWeekly savedActivity = GroupActivityWeekly.builder()
			.id(1L)
			.studyGroup(testGroup)
			.weekStart(weekStart)
			.weekEnd(weekEnd)
			.submissions(10)
			.comments(5)
			.activeScore(17.5)
			.build();

		when(groupActivityWeeklyRepository.save(any(GroupActivityWeekly.class))).thenReturn(savedActivity);

		// when
		recommendationBatchService.calculateAndSaveMostActiveStudy();

		// then
		verify(studyGroupRepository).findAll();
		verify(solutionRepository).countByStudyGroupAndSolvedDateTimeBetween(eq(testGroup), any(), any());
		verify(solutionCommentRepository).countByStudyGroupAndCreatedAtBetween(eq(testGroup), any(), any());
		verify(groupActivityWeeklyRepository).save(any(GroupActivityWeekly.class));
		verify(studyGroupTagRepository).save(any(StudyGroupTag.class));
	}

	@Test
	@DisplayName("스터디 그룹이 없을 때 예외 처리")
	void calculateAndSaveMostActiveStudy_NoGroups() {
		// given
		when(studyGroupRepository.findAll()).thenReturn(List.of());

		// when & then
		assertThatCode(() -> recommendationBatchService.calculateAndSaveMostActiveStudy())
			.doesNotThrowAnyException();

		verify(studyGroupRepository).findAll();
		verifyNoInteractions(solutionRepository);
		verifyNoInteractions(groupActivityWeeklyRepository);
		verifyNoInteractions(studyGroupTagRepository);
	}

	@Test
	@DisplayName("최근 가입률이 높은 스터디 계산 및 저장 성공")
	void calculateAndSaveHighJoinRateStudy_Success() {
		// given
		List<StudyGroup> groups = List.of(testGroup);
		LocalDate today = LocalDate.now();
		LocalDate thirtyDaysAgo = today.minusDays(30);

		when(studyGroupRepository.findAll()).thenReturn(groups);
		when(groupMemberRepository.countByStudyGroupAndJoinDateBefore(eq(testGroup), eq(thirtyDaysAgo)))
			.thenReturn(20);
		when(groupMemberRepository.countByStudyGroupAndJoinDateBetween(eq(testGroup), eq(thirtyDaysAgo), any()))
			.thenReturn(5);

		GroupJoinMonthlyRolling savedJoinInfo = GroupJoinMonthlyRolling.builder()
			.id(1L)
			.studyGroup(testGroup)
			.joinRate(0.25)
			.build();

		when(groupJoinMonthlyRollingRepository.save(any(GroupJoinMonthlyRolling.class))).thenReturn(savedJoinInfo);

		// when
		recommendationBatchService.calculateAndSaveHighJoinRateStudy();

		// then
		verify(studyGroupRepository).findAll();
		verify(groupMemberRepository).countByStudyGroupAndJoinDateBefore(eq(testGroup), eq(thirtyDaysAgo));
		verify(groupMemberRepository).countByStudyGroupAndJoinDateBetween(eq(testGroup), eq(thirtyDaysAgo), any());
		verify(groupJoinMonthlyRollingRepository).save(any(GroupJoinMonthlyRolling.class));
		verify(studyGroupTagRepository).save(any(StudyGroupTag.class));
	}

	@Test
	@DisplayName("난이도 정보 계산 및 저장 성공")
	void calculateAndSaveDifficultyInfo_Success() {
		// given
		List<StudyGroup> groups = List.of(testGroup);
		List<User> users = List.of(testUser);
		List<Problem> problems = List.of(testProblem);
		LocalDate today = LocalDate.now();
		LocalDate thirtyDaysAgo = today.minusDays(30);

		when(studyGroupRepository.findAll()).thenReturn(groups);
		when(problemRepository.findAllByStudyGroupAndStartDateBetween(eq(testGroup), eq(thirtyDaysAgo), eq(today)))
			.thenReturn(problems);

		when(userRepository.findAll()).thenReturn(users);
		when(solutionRepository.findAverageProblemLevelForUserInPeriod(eq(testUser), any(), any()))
			.thenReturn(3.5);

		// when
		recommendationBatchService.calculateAndSaveDifficultyInfo();

		// then
		verify(studyGroupRepository).findAll();
		verify(problemRepository).findAllByStudyGroupAndStartDateBetween(eq(testGroup), eq(thirtyDaysAgo), eq(today));
		verify(groupDifficultyMonthlyRollingRepository).save(any(GroupDifficultyMonthlyRolling.class));

		verify(userRepository).findAll();
		verify(solutionRepository).findAverageProblemLevelForUserInPeriod(eq(testUser), any(), any());
		verify(userDifficultyMonthlyRollingRepository).save(any(UserDifficultyMonthlyRolling.class));
	}
}
