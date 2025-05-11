package com.gamzabat.algohub.feature.solution.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.ranking.service.RankingService;
import com.gamzabat.algohub.feature.group.ranking.service.RankingUpdateService;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.enums.NotificationCategory;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.domain.SolutionComment;
import com.gamzabat.algohub.feature.solution.dto.CreateSolutionRequest;
import com.gamzabat.algohub.feature.solution.dto.GetCurrentSolvingStatusResponse;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionWithGroupIdResponse;
import com.gamzabat.algohub.feature.solution.dto.GetSolvingStatusPerProblemResponse;
import com.gamzabat.algohub.feature.solution.enums.ProgressCategory;
import com.gamzabat.algohub.feature.solution.exception.CannotFoundSolutionException;
import com.gamzabat.algohub.feature.solution.repository.SolutionCommentRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionService {
	private final SolutionRepository solutionRepository;
	private final ProblemRepository problemRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final SolutionCommentRepository commentRepository;
	private final RankingService rankingService;
	private final RankingUpdateService rankingUpdateService;
	private final SolutionCommentRepository solutionCommentRepository;

	@Transactional(readOnly = true)
	public Page<GetSolutionResponse> getSolutionList(User user, Long problemId, String nickname,
		String language, String result, Pageable pageable) {
		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));

		StudyGroup group = studyGroupRepository.findById(problem.getStudyGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다.");
		}

		Page<Solution> solutions = solutionRepository.findAllFilteredSolutions(problem, nickname, language, result,
			pageable);

		return solutions.map(solution -> this.getGetSolutionResponse(user, solution));

	}

	@Transactional(readOnly = true)
	public GetSolutionResponse getSolution(User user, Long solutionId) {
		Solution solution = solutionRepository.findById(solutionId)
			.orElseThrow(() -> new CannotFoundSolutionException("존재하지 않는 풀이 입니다."));

		StudyGroup group = solution.getProblem().getStudyGroup();

		if (groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			return getGetSolutionResponse(user, solution);
		} else {
			throw new UserValidationException("해당 풀이를 확인 할 권한이 없습니다.");
		}
	}

	@Transactional(readOnly = true)
	public Page<GetSolutionResponse> getMySolutionsInGroupInProgress(User user, Long groupId, Integer problemNumber,
		String language,
		String result, Pageable pageable) {
		StudyGroup group = validateGroupAndMember(user, groupId);

		Page<GetSolutionResponse> inProgressSolutions = solutionRepository.findAllFilteredMySolutionsInGroup(user,
				group, problemNumber, language, result, ProgressCategory.IN_PROGRESS, pageable)
			.map(solution -> this.getGetSolutionResponse(user, solution));

		log.info("success to get my in-progress solutions in group {}", groupId);
		return inProgressSolutions;
	}

	@Transactional(readOnly = true)
	public Page<GetSolutionResponse> getMySolutionsInGroupExpired(User user, Long groupId, Integer problemNumber,
		String language,
		String result, Pageable pageable) {
		StudyGroup group = validateGroupAndMember(user, groupId);

		Page<GetSolutionResponse> expiredSolutions = solutionRepository.findAllFilteredMySolutionsInGroup(user, group,
				problemNumber, language, result, ProgressCategory.EXPIRED, pageable)
			.map(solution -> this.getGetSolutionResponse(user, solution));

		log.info("success to get my expired solutions in group {}", groupId);
		return expiredSolutions;
	}

	@Transactional(readOnly = true)
	public Page<GetSolutionWithGroupIdResponse> getMySolutionsInProgress(User user, Integer problemNumber,
		String language,
		String result,
		Pageable pageable) {
		Page<GetSolutionWithGroupIdResponse> inProgressSolutions = solutionRepository.findAllFilteredMySolutions(user,
				problemNumber,
				language,
				result, ProgressCategory.IN_PROGRESS, pageable)
			.map(solution -> this.getGetSolutionWithGroupIdResponse(user, solution));
		log.info("success to get my in-progress solutions.");
		return inProgressSolutions;
	}

	@Transactional(readOnly = true)
	public Page<GetSolutionWithGroupIdResponse> getMySolutionsExpired(User user, Integer problemNumber, String language,
		String result,
		Pageable pageable) {
		Page<GetSolutionWithGroupIdResponse> expiredSolutions = solutionRepository.findAllFilteredMySolutions(user,
				problemNumber,
				language,
				result, ProgressCategory.EXPIRED, pageable)
			.map(solution -> this.getGetSolutionWithGroupIdResponse(user, solution));
		log.info("success to get my expired solutions.");
		return expiredSolutions;
	}

	@Transactional(readOnly = true)
	public List<GetCurrentSolvingStatusResponse> getCurrentSolvingStatuses(User user, Long groupId) {
		StudyGroup group = validateGroupAndMember(user, groupId);

		List<Problem> inProgressProblems = problemRepository.findAllInProgressProblem(group);
		List<GroupMember> members = groupMemberRepository.findAllByStudyGroup(group);

		Map<GroupMember, SolvedStatusResult> solvedStatuses = calculateMemberStatusRanks(members, inProgressProblems);

		return createSolvingStatusResponses(solvedStatuses);
	}

	private Map<GroupMember, SolvedStatusResult> calculateMemberStatusRanks(List<GroupMember> members,
		List<Problem> inProgressProblems) {
		Map<GroupMember, SolvedStatusResult> ranks = new LinkedHashMap<>();

		for (GroupMember member : members) {
			int totalSubmissionCount = 0;
			Duration totalPassedTime = Duration.ZERO;
			List<GetSolvingStatusPerProblemResponse> statusResponses = new ArrayList<>();

			for (Problem problem : inProgressProblems) {
				List<Solution> solutions = solutionRepository.findAllByUserAndProblem(member.getUser(), problem);
				int submissionCount = solutions.size();
				Long firstCorrectSolutionId = null;
				String firstCorrectDuration = "--";
				boolean solved = false;

				Optional<Solution> firstCorrectSolution = solutions.stream()
					.filter(solution -> isCorrect(solution.getResult()))
					.min(Comparator.comparing(Solution::getSolvedDateTime));

				if (firstCorrectSolution.isPresent()) {
					Solution solution = firstCorrectSolution.get();

					firstCorrectSolutionId = solution.getId();

					Duration duration = calculateGap(problem, solution);
					totalPassedTime = totalPassedTime.plus(duration);
					firstCorrectDuration = convertToSolvedTimeFormat(duration);
					solved = true;
				}
				totalSubmissionCount += submissionCount;

				statusResponses.add(new GetSolvingStatusPerProblemResponse(
					problem.getId(), firstCorrectSolutionId,
					submissionCount, firstCorrectDuration, solved
				));
			}

			float totalScore = calculateTotalScore(totalSubmissionCount, totalPassedTime);
			String formattedPassedTime = convertToSolvedTimeFormat(totalPassedTime);
			ranks.put(member,
				new SolvedStatusResult(totalScore, totalSubmissionCount, formattedPassedTime, statusResponses));
		}
		return ranks;
	}

	private Duration calculateGap(Problem problem, Solution solution) {
		LocalDateTime startDate = problem.getStartDate().atStartOfDay();
		LocalDateTime solvedDateTime = solution.getSolvedDateTime();
		return Duration.between(startDate, solvedDateTime);
	}

	private float calculateTotalScore(int totalSubmissionCount, Duration totalPassedTime) {
		float totalScore = 0;
		if (!(totalSubmissionCount == 0 || totalPassedTime.isZero())) {
			long minutes = totalPassedTime.toMinutes();
			totalScore = (float)1 / (totalSubmissionCount * minutes);
		}
		return totalScore;
	}

	private List<GetCurrentSolvingStatusResponse> createSolvingStatusResponses(
		Map<GroupMember, SolvedStatusResult> ranks) {
		List<GroupMember> memberOrders = ranks.keySet().stream()
			.sorted((m1, m2) -> Float.compare(ranks.get(m2).totalScore, ranks.get(m1).totalScore))
			.toList();

		List<GetCurrentSolvingStatusResponse> responses = new ArrayList<>();
		for (int i = 0; i < memberOrders.size(); i++) {
			GroupMember member = memberOrders.get(i);
			responses.add(new GetCurrentSolvingStatusResponse(
				i + 1, member.getUser().getNickname(),
				ranks.get(member).totalSubmissionCount,
				ranks.get(member).totalPassedTime,
				ranks.get(member).problems
			));
		}
		return responses;
	}

	private void sendNewSolutionNotification(StudyGroup group, GroupMember solver, Problem problem) {
		List<GroupMember> groupMembers = groupMemberRepository.findAllByStudyGroup(group).stream()
			.filter(member -> !member.getId().equals(solver.getId()))
			.toList();

		notificationService.sendNotificationToMembers(
			group,
			groupMembers,
			problem,
			null,
			NotificationCategory.NEW_SOLUTION_POSTED,
			NotificationCategory.NEW_SOLUTION_POSTED.getMessage(solver.getUser().getNickname())
		);
	}

	private String convertToSolvedTimeFormat(Duration duration) {
		long totalMinutes = duration.toMinutes();
		long hours = totalMinutes / 60;
		long minutes = totalMinutes % 60;
		return String.format("%d:%02d", hours, minutes);
	}

	private GetSolutionWithGroupIdResponse getGetSolutionWithGroupIdResponse(User user, Solution solution) {
		Integer correctCount = getCorrectCount(solution);
		Integer submitMemberCount = solutionRepository.countDistinctUsersByProblem(solution.getProblem());
		Integer totalMemberCount = groupMemberRepository.countMembersByStudyGroup(getGroup(solution)) + 1;
		Integer accuracy = calculateAccuracy(submitMemberCount, correctCount);
		long commentCount = commentRepository.countCommentsBySolutionId(solution.getId());
		boolean isRead = true;

		if (isMySolution(user, solution)) {
			isRead = isAllCommentsRead(solution);
		}
		return GetSolutionWithGroupIdResponse.toDTO(solution, accuracy, submitMemberCount, totalMemberCount,
			commentCount, isRead);
	}

	private GetSolutionResponse getGetSolutionResponse(User user, Solution solution) {
		Integer correctCount = getCorrectCount(solution);
		Integer submitMemberCount = solutionRepository.countDistinctUsersByProblem(solution.getProblem());
		Integer totalMemberCount = groupMemberRepository.countMembersByStudyGroup(getGroup(solution)) + 1;
		Integer accuracy = calculateAccuracy(submitMemberCount, correctCount);
		long commentCount = commentRepository.countCommentsBySolutionId(solution.getId());
		boolean isRead = true;

		if (isMySolution(user, solution)) {
			isRead = isAllCommentsRead(solution);
		}

		return GetSolutionResponse.toDTO(solution, accuracy, submitMemberCount, totalMemberCount, commentCount, isRead);
	}

	private Integer getCorrectCount(Solution solution) {
		return solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(
			solution.getProblem().getId(),
			BOJResultConstants.CORRECT);
	}

	@Transactional
	public void createSolution(CreateSolutionRequest request) {

		List<User> users = userRepository.findAllByBjNickname(request.userName());
		if (users.isEmpty()) {
			log.warn("user {} not found for create solution", request.userName());
			throw new UserValidationException("해당 아이디로 등록된 백준 유저는 없습니다.");
		}

		final LocalDateTime now = LocalDateTime.now();

		for (User user : users) {
			List<Problem> problems = problemRepository.findValidProblemsByNumberAndUser(
				request.problemNumber(), LocalDate.from(now), user);

			if (problems.isEmpty()) {
				log.warn("problem {} not found for create solution, user : {}", request.problemNumber(), user.getId());
			}

			for (Problem problem : problems) {
				final boolean isFirstCorrect =
					isCorrect(request.result()) && !solutionRepository.existsByUserAndProblemAndResult(user, problem,
						BOJResultConstants.CORRECT);

				Long solutionId = solutionRepository.save(Solution.builder()
					.problem(problem)
					.user(user)
					.content(request.code())
					.memoryUsage(request.memoryUsage())
					.executionTime(request.executionTime())
					.language(request.codeType())
					.codeLength(request.codeLength())
					.result(request.result())
					.solvedDateTime(now)
					.build()
				).getId();

				GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, problem.getStudyGroup())
					.orElseThrow(() -> new IllegalStateException("Logic Error"));

				if (isFirstCorrect) {
					rankingService.updateScore(groupMember, problem.getEndDate(), now);
					rankingUpdateService.updateRanking(problem.getStudyGroup());
				}

				sendNewSolutionNotification(problem.getStudyGroup(), groupMember, problem);

				log.info("success to create solution for user: {}, solutionId: {}", user.getId(), solutionId);

			}

		}

	}

	private StudyGroup validateGroupAndMember(User user, Long groupId) {
		StudyGroup group = studyGroupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹입니다."));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹입니다.");
		}
		return group;
	}

	private boolean isCorrect(String result) {
		return result.equals(BOJResultConstants.CORRECT) || result.endsWith("점");
	}

	private Integer calculateAccuracy(Integer submitMemberCount, Integer correctCount) {
		if (submitMemberCount == 0)
			return 0;

		Double tempCorrectCount = correctCount.doubleValue();
		Double tempSubmitMemberCount = submitMemberCount.doubleValue();
		Double tempAccuracy = ((tempCorrectCount / tempSubmitMemberCount) * 100);
		return tempAccuracy.intValue();
	}

	private StudyGroup getGroup(Solution solution) {
		return solution.getProblem().getStudyGroup();
	}

	private boolean isMySolution(User user, Solution solution) {
		return solution.getUser().getId().equals(user.getId());
	}

	private boolean isAllCommentsRead(Solution solution) {
		List<SolutionComment> comments = solutionCommentRepository.findAllBySolution(solution);

		for (SolutionComment solutionComment : comments) {
			if (!solutionComment.isRead())
				return false;
		}

		return true;
	}

	private record SolvedStatusResult(float totalScore,
									  int totalSubmissionCount,
									  String totalPassedTime,
									  List<GetSolvingStatusPerProblemResponse> problems) {
	}
}
