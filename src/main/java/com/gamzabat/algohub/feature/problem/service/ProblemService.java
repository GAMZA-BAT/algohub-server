package com.gamzabat.algohub.feature.problem.service;

import static com.gamzabat.algohub.constants.ApiConstants.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundProblemException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.enums.NotificationCategory;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.exception.NotBojLinkException;
import com.gamzabat.algohub.feature.problem.exception.SolvedAcApiErrorException;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {
	private final SolutionRepository solutionRepository;
	private final ProblemRepository problemRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final NotificationService notificationService;
	private final RestTemplate restTemplate;
	private final NotificationRepository notificationRepository;

	@Transactional
	public void createProblem(User user, Long groupId, CreateProblemRequest request) {
		StudyGroup group = getGroup(groupId);
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isParticipant(groupMember)) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제 생성 권한이 없습니다. 방장, 부방장일 경우에만 생성이 가능합니다.");
		}

		String number = getProblemId(request);
		JsonNode apiResult = fetchProblemDetails(number);
		int level = getProblemLevel(apiResult);
		String title = getProblemTitle(apiResult);

		Problem problem = problemRepository.save(Problem.builder()
			.studyGroup(group)
			.link(request.link())
			.number(Integer.parseInt(number))
			.title(title)
			.level(level)
			.startDate(request.startDate())
			.endDate(request.endDate())
			.build());

		if (request.startDate().equals(LocalDate.now()))
			notificationService.sendNotificationToMembers(
				group,
				groupMemberRepository.findAllByStudyGroup(group),
				problem,
				null,
				NotificationCategory.PROBLEM_STARTED,
				NotificationCategory.PROBLEM_STARTED.getMessage(title)
			);

		log.info("success to create problem user_id={} , group_id = {}", user.getId(), groupId);
	}

	@Transactional
	public void editProblem(User user, Long problemId, EditProblemRequest request) {
		Problem problem = getProblem(problemId);
		StudyGroup group = getGroup(problem.getStudyGroup().getId());
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isParticipant(groupMember)) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제 수정 권한이 없습니다. 방장, 부방장일 경우에만 수정이 가능합니다.");
		}

		checkProblemValidation(problem);

		if (request.startDate() != null) {
			checkProblemStartDate(request, problem);
			problem.editProblemStartDate(request.startDate());
		}
		if (request.endDate() != null) {
			checkProblemEndDate(request, problem);
			problem.editProblemEndDate(request.endDate());
		}

		log.info("success to edit problem deadline user_id={} , problem_id = {}", user.getId(), problemId);
	}

	private void checkProblemValidation(Problem problem) {
		if (problem.getEndDate().isBefore(LocalDate.now())) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(),
				"문제 수정이 불가합니다. : 이미 종료된 문제입니다.");
		}
		if (problem.getStartDate().isBefore(LocalDate.now()) || problem.getStartDate().equals(LocalDate.now())) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(),
				"문제 수정이 불가합니다. : 이미 진행 중인 문제입니다.");
		}

	}

	private void checkProblemEndDate(EditProblemRequest request, Problem problem) {
		if (request.endDate().isBefore(problem.getStartDate()))
			throw new ProblemValidationException(HttpStatus.BAD_REQUEST.value(),
				"문제 마감 날짜는 시작 날짜 이전으로 수정할 수 없습니다.");
		if (request.endDate().isBefore(LocalDate.now()))
			throw new ProblemValidationException(HttpStatus.BAD_REQUEST.value(),
				"문제 마감 날짜는 오늘 이전의 날짜로 수정할 수 없습니다.");
	}

	private void checkProblemStartDate(EditProblemRequest request, Problem problem) {
		if (request.startDate().isBefore(LocalDate.now()))
			throw new ProblemValidationException(HttpStatus.BAD_REQUEST.value(),
				"문제 시작 날짜는 오늘 이전의 날짜로 수정할 수 없습니다.");
		if (request.startDate().isAfter(problem.getEndDate()))
			throw new ProblemValidationException(HttpStatus.BAD_REQUEST.value(),
				"문제 시작 날짜는 마감 날짜 이후로 수정할 수 없습니다.");
	}

	@Transactional(readOnly = true)
	public Page<GetProblemResponse> getInProgressProblems(User user, Long groupId, Boolean unsolvedOnly,
		Pageable pageable) {
		StudyGroup group = getGroup(groupId);
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다.");
		}

		Page<Problem> problems = problemRepository.findAllInProgressProblem(user, group, unsolvedOnly,
			pageable);

		return problems.map(problem -> getGetProblemResponse(user, group, problem, unsolvedOnly));
	}

	@Transactional(readOnly = true)
	public Page<GetProblemResponse> getExpiredProblems(User user, Long groupId, Pageable pageable) {
		StudyGroup group = getGroup(groupId);
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다.");
		}

		Page<Problem> problems = problemRepository.findAllExpiredProblem(group, pageable);

		return problems.map(problem -> getGetProblemResponse(user, group, problem, false));
	}

	private GetProblemResponse getGetProblemResponse(User user, StudyGroup group, Problem problem,
		boolean unsolvedOnly) {
		boolean solved = unsolvedOnly ? false : solutionRepository.existsByUserAndProblemAndResult(user, problem,
			BOJResultConstants.CORRECT);
		Integer correctCount = solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(problem.getId(),
			BOJResultConstants.CORRECT);
		Integer submitMemberCount = solutionRepository.countDistinctUsersByProblem(problem);
		Integer groupMemberCount = groupMemberRepository.countMembersByStudyGroup(group);
		Integer accuracy = calculateAccuracy(submitMemberCount, correctCount);

		return new GetProblemResponse(
			problem.getTitle(),
			problem.getId(),
			problem.getLink(),
			problem.getStartDate(),
			problem.getEndDate(),
			problem.getLevel(),
			solved, submitMemberCount, groupMemberCount, accuracy);
	}

	@Transactional
	public void deleteProblem(User user, Long problemId) {
		Problem problem = getProblem(problemId);
		StudyGroup group = getGroup(problem.getStudyGroup().getId());
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isParticipant(groupMember)) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제 삭제 권한이 없습니다. 방장, 부방장일 경우에만 삭제가 가능합니다.");
		}

		solutionRepository.deleteAllByProblem(problem);
		problemRepository.delete(problem);
		notificationRepository.deleteAllByProblem(problem);
		log.info("success to delete problem user_id={} , problem_id = {}", user.getId(), problemId);
	}

	@Transactional(readOnly = true)
	public List<GetProblemResponse> getDeadlineReachedProblemList(User user, Long groupId) {
		StudyGroup group = getGroup(groupId);
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group))
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다.");

		List<Problem> problems = problemRepository.findAllByStudyGroupAndEndDateBetween(group, LocalDate.now(),
			LocalDate.now().plusDays(1));
		problems.sort(Comparator.comparing(Problem::getEndDate));

		return problems.stream().map(problem -> {
			Integer correctCount = solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(problem.getId(),
				BOJResultConstants.CORRECT);
			Integer submitMemberCount = solutionRepository.countDistinctUsersByProblem(problem);
			Integer groupMemberCount = groupMemberRepository.countMembersByStudyGroup(group);
			Integer accuracy = calculateAccuracy(submitMemberCount, correctCount);

			return new GetProblemResponse(
				problem.getTitle(),
				problem.getId(),
				problem.getLink(),
				problem.getStartDate(),
				problem.getEndDate(),
				problem.getLevel(),
				solutionRepository.existsByUserAndProblemAndResult(user, problem, BOJResultConstants.CORRECT),
				submitMemberCount,
				groupMemberCount,
				accuracy);
		}).toList();
	}

	@Transactional(readOnly = true)
	public Page<GetProblemResponse> getQueuedProblems(User user, Long groupId, Pageable pageable) {
		StudyGroup group = getGroup(groupId);
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isParticipant(groupMember)) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(),
				"예정 문제를 조회할 권한이 없습니다. : 그룹의 방장과 부방장만 볼 수 있습니다.");
		}

		Page<Problem> problems = problemRepository.findAllQueuedProblem(group, pageable);
		return problems
			.map(problem -> {
				String title = problem.getTitle();
				Long problemId = problem.getId();
				String link = problem.getLink();
				LocalDate startDate = problem.getStartDate();
				LocalDate endDate = problem.getEndDate();
				Integer level = problem.getLevel();
				boolean solved = false;
				Integer submitMemberCount = 0;
				Integer groupMemberCount = groupMemberRepository.countMembersByStudyGroup(group);
				Integer accuracy = 0;

				return new GetProblemResponse(title, problemId, link, startDate, endDate, level, solved,
					submitMemberCount,
					groupMemberCount, accuracy);
			});
	}

	@Transactional(readOnly = true)
	public GetProblemResponse getProblem(User user, Long problemId) {
		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new CannotFoundProblemException("존재하지 않는 문제입니다."));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, problem.getStudyGroup()))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹입니다.");

		boolean solved = solutionRepository.existsByUserAndProblemAndResult(user, problem,
			BOJResultConstants.CORRECT);
		Integer correctCount = solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(problem.getId(),
			BOJResultConstants.CORRECT);
		Integer submitMemberCount = solutionRepository.countDistinctUsersByProblem(problem);
		Integer groupMemberCount =
			groupMemberRepository.countMembersByStudyGroup(problem.getStudyGroup());
		Integer accuracy = calculateAccuracy(submitMemberCount, correctCount);

		GetProblemResponse response = new GetProblemResponse(
			problem.getTitle(),
			problem.getId(),
			problem.getLink(),
			problem.getStartDate(),
			problem.getEndDate(),
			problem.getLevel(),
			solved, submitMemberCount, groupMemberCount, accuracy);
		log.info("success to get problem. problemId:{}", problemId);
		return response;
	}

	@Transactional
	@Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
	public void dailyProblemScheduler() {
		LocalDate now = LocalDate.now();
		notifyProblemStartsToday(now);
		notifyProblemEndsToday(now);
	}

	private void notifyProblemStartsToday(LocalDate now) {
		List<Problem> problems = problemRepository.findAllByStartDate(now);
		for (Problem problem : problems) {
			notificationService.sendNotificationToMembers(
				problem.getStudyGroup(),
				groupMemberRepository.findAllByStudyGroup(problem.getStudyGroup()),
				problem,
				null,
				NotificationCategory.PROBLEM_STARTED,
				NotificationCategory.PROBLEM_STARTED.getMessage(problem.getTitle())
			);
		}
	}

	private void notifyProblemEndsToday(LocalDate now) {
		List<Problem> problems = problemRepository.findAllByEndDate(now);
		for (Problem problem : problems) {
			notificationService.sendNotificationToMembers(
				problem.getStudyGroup(),
				groupMemberRepository.findAllByStudyGroup(problem.getStudyGroup()),
				problem,
				null,
				NotificationCategory.PROBLEM_DEADLINE_REACHED,
				NotificationCategory.PROBLEM_DEADLINE_REACHED.getMessage(problem.getTitle())
			);
		}
	}

	private Problem getProblem(Long problemId) {
		return problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));
	}

	private StudyGroup getGroup(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
	}

	private JsonNode fetchProblemDetails(String problemId) {
		String url = SOLVED_AC_PROBLEM_API_URL + problemId;

		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String responseBody = responseEntity.getBody();
			if (responseBody == null || responseBody.isEmpty()) {
				log.error("Unexpected solved.ac API response format : " + responseBody);
				throw new SolvedAcApiErrorException(HttpStatus.SERVICE_UNAVAILABLE.value(),
					"solved.ac API로부터 예상치 못한 응답을 받았습니다.");
			}

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(responseBody);
			if (!root.isArray()) {
				log.error("Unexpected solved.ac API response format : " + responseBody);
				throw new SolvedAcApiErrorException(HttpStatus.SERVICE_UNAVAILABLE.value(),
					"solved.ac API로부터 예상치 못한 응답을 받았습니다.");
			}

			if (root.isEmpty())
				throw new SolvedAcApiErrorException(HttpStatus.BAD_REQUEST.value(), "백준에 유효하지 않은 문제입니다.");

			return root.get(0);
		} catch (JsonProcessingException e) {
			log.error("Json processing error : " + e.getMessage());
			throw new SolvedAcApiErrorException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"서버에서 solved.ac API JSON 응답 처리 중 오류가 발생했습니다.");
		}
	}

	private int getProblemLevel(JsonNode problemDetails) {
		return problemDetails.get("level").asInt();
	}

	private String getProblemTitle(JsonNode problemDetails) {
		return problemDetails.get("titleKo").asText();
	}

	private String getProblemId(CreateProblemRequest request) {
		String url = request.link();
		String[] parts = url.split("/");
		if (parts.length < 3 || !parts[2].equals(BOJ_PROBLEM_URL))
			throw new NotBojLinkException(HttpStatus.BAD_REQUEST.value(), "백준 링크가 아닙니다");
		return parts[parts.length - 1];
	}

	private Integer calculateAccuracy(Integer submitMemberCount, Integer correctCount) {
		if (submitMemberCount == 0)
			return 0;

		Double tempCorrectCount = correctCount.doubleValue();
		Double tempSubmitMemberCount = submitMemberCount.doubleValue();
		Double tempAccuracy = ((tempCorrectCount / tempSubmitMemberCount) * 100);
		return tempAccuracy.intValue();
	}
}
