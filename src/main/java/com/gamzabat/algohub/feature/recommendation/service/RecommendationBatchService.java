package com.gamzabat.algohub.feature.recommendation.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.recommendation.domain.GroupActivityWeekly;
import com.gamzabat.algohub.feature.recommendation.domain.GroupDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.domain.GroupJoinMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.domain.StudyGroupTag;
import com.gamzabat.algohub.feature.recommendation.domain.TagType;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecommendationBatchService {

	// 스터디, 유저, 활동 관련 Repository
	private final StudyGroupRepository studyGroupRepository;
	private final UserRepository userRepository;
	private final SolutionRepository solutionRepository;
	private final SolutionCommentRepository solutionCommentRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final ProblemRepository problemRepository;

	// 집계 데이터 저장용 Repository
	private final GroupActivityWeeklyRepository groupActivityWeeklyRepository;
	private final GroupJoinMonthlyRollingRepository groupJoinMonthlyRollingRepository;
	private final GroupDifficultyMonthlyRollingRepository groupDifficultyMonthlyRollingRepository;
	private final UserDifficultyMonthlyRollingRepository userDifficultyMonthlyRollingRepository;
	private final StudyGroupTagRepository studyGroupTagRepository;

	/**
	 * '이번주 가장 많이 활동한 스터디'를 계산하고 스냅샷을 저장합니다.
	 */
	public void calculateAndSaveMostActiveStudy() {
		log.info("Batch Job Started: MOST_ACTIVE_THIS_WEEK");

		// 1. 집계 기간 설정 (지난주 월요일 00:00 ~ 일요일 23:59)
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
			.minusWeeks(1)
			.toLocalDate()
			.atStartOfDay();
		LocalDateTime weekEnd = weekStart.plusWeeks(1).minusNanos(1);

		List<StudyGroup> allGroups = studyGroupRepository.findAll();
		if (allGroups.isEmpty()) {
			log.warn("No study groups found. Skipping MOST_ACTIVE_THIS_WEEK calculation.");
			return;
		}

		// 2. 각 스터디 그룹별 활동 점수 계산 및 저장
		Optional<GroupActivityWeekly> topGroupActivity = allGroups.stream()
			.map(group -> {
				Long submissions = solutionRepository.countByStudyGroupAndSolvedDateTimeBetween(group, weekStart,
					weekEnd);
				Long comments = solutionCommentRepository.countByStudyGroupAndCreatedAtBetween(group, weekStart,
					weekEnd);
				double activeScore = (submissions * 1.0) + (comments * 1.5);

				return groupActivityWeeklyRepository.save(GroupActivityWeekly.builder()
					.studyGroup(group)
					.weekStart(weekStart)
					.weekEnd(weekEnd)
					.submissions(submissions.intValue())
					.comments(comments.intValue())
					.activeScore(activeScore)
					.build());
			})
			.max(Comparator.comparing(GroupActivityWeekly::getActiveScore));

		// 3. 가장 점수가 높은 스터디를 스냅샷으로 저장
		topGroupActivity.ifPresent(activity -> {
			StudyGroupTag snapshot = StudyGroupTag.builder()
				.studyGroup(activity.getStudyGroup())
				.tagType(TagType.MOST_ACTIVE_THIS_WEEK)
				.score(activity.getActiveScore())
				.firstAchievedAt(LocalDateTime.now())
				.windowStart(weekStart)
				.windowEnd(weekEnd)
				.computedAt(now)
				.build();
			studyGroupTagRepository.save(snapshot);
		});
	}

	/**
	 * '최근 가입률이 높은 스터디'를 계산하고 스냅샷을 저장합니다.
	 */
	public void calculateAndSaveHighJoinRateStudy() {
		log.info("Batch Job Started: HIGH_JOIN_RATE_RECENT");

		// 1. 집계 기간 설정 (최근 30일)
		LocalDate today = LocalDate.now();
		LocalDate thirtyDaysAgo = today.minusDays(30);
		LocalDateTime windowStart = thirtyDaysAgo.atStartOfDay();
		LocalDateTime windowEnd = today.atStartOfDay().minusNanos(1);

		List<StudyGroup> allGroups = studyGroupRepository.findAll();
		if (allGroups.isEmpty()) {
			return;
		}

		// 2. 각 스터디 그룹별 가입률 계산 및 저장
		Optional<GroupJoinMonthlyRolling> topJoinRateGroup = allGroups.stream()
			.map(group -> {
				Integer membersBeforeWindow = groupMemberRepository.countByStudyGroupAndJoinDateBefore(group,
					thirtyDaysAgo);
				Integer newMembers = groupMemberRepository.countByStudyGroupAndJoinDateBetween(group, thirtyDaysAgo,
					today.minusDays(1)); // 오늘 가입자는 제외
				double joinRate = (double)newMembers / Math.max(1, membersBeforeWindow);

				return groupJoinMonthlyRollingRepository.save(GroupJoinMonthlyRolling.builder()
					.studyGroup(group)
					.windowStart(windowStart)
					.windowEnd(windowEnd)
					.newMembers(newMembers)
					.membersBeforeWindow(membersBeforeWindow)
					.joinRate(joinRate)
					.build());
			})
			.max(Comparator.comparing(GroupJoinMonthlyRolling::getJoinRate));

		// 3. 가장 가입률이 높은 스터디를 스냅샷으로 저장
		topJoinRateGroup.ifPresent(joinInfo -> {
			StudyGroupTag snapshot = StudyGroupTag.builder()
				.studyGroup(joinInfo.getStudyGroup())
				.tagType(TagType.HIGH_JOIN_RATE_RECENT)
				.score(joinInfo.getJoinRate())
				.firstAchievedAt(LocalDateTime.now())
				.windowStart(windowStart)
				.windowEnd(windowEnd)
				.computedAt(LocalDateTime.now())
				.build();
			studyGroupTagRepository.save(snapshot);
		});
	}

	/**
	 * '스터디 및 사용자별 난이도'를 계산하여 저장합니다. (스냅샷 X)
	 */
	public void calculateAndSaveDifficultyInfo() {
		log.info("Batch Job Started: SIMILAR_DIFFICULTY");

		// 1. 집계 기간 설정 (최근 30일)
		LocalDate today = LocalDate.now();
		LocalDate thirtyDaysAgo = today.minusDays(30);
		LocalDateTime windowStart = thirtyDaysAgo.atStartOfDay();
		LocalDateTime windowEnd = today.atStartOfDay().minusNanos(1);

		// 2. 스터디별 평균 난이도 계산 및 저장
		List<StudyGroup> allGroups = studyGroupRepository.findAll();
		allGroups.forEach(group -> {
			List<Problem> problems = problemRepository.findAllByStudyGroupAndStartDateBetween(group, thirtyDaysAgo,
				today);
			double avgDifficulty = problems.stream()
				.mapToInt(Problem::getLevel)
				.average()
				.orElse(0.0);

			groupDifficultyMonthlyRollingRepository.save(GroupDifficultyMonthlyRolling.builder()
				.studyGroup(group)
				.windowStart(windowStart)
				.windowEnd(windowEnd)
				.avgDifficulty(avgDifficulty)
				.build());
		});

		// 3. 사용자별 평균 난이도 계산 및 저장
		List<User> allUsers = userRepository.findAll();
		allUsers.forEach(user -> {
			Double avgDifficulty = solutionRepository.findAverageProblemLevelForUserInPeriod(user, windowStart,
				windowEnd);

			userDifficultyMonthlyRollingRepository.save(UserDifficultyMonthlyRolling.builder()
				.user(user)
				.windowStart(windowStart)
				.windowEnd(windowEnd)
				.avgDifficulty(avgDifficulty)
				.build());
		});
	}
}
