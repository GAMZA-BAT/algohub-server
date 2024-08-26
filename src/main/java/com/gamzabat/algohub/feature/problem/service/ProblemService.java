package com.gamzabat.algohub.feature.problem.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemListsResponse;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.exception.NotBojLinkException;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
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

	private static void checkOwnerPermission(User user, StudyGroup group, String permission) {
		if (!group.getOwner().getId().equals(user.getId()))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "문제에 대한 권한이 없습니다. : " + permission);
	}

	@Transactional
	public void createProblem(User user, CreateProblemRequest request) {
		StudyGroup group = getGroup(request.groupId());
		Optional<GroupMember> groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group);

		Boolean isOwner = (group.getOwner().getId().equals(user.getId()) && groupMember.isEmpty());
		Boolean isAdmin = (!groupMember.isEmpty() && groupMember.get().getRole().equals(RoleOfGroupMember.ADMIN));
		Boolean isGroupMember = groupMember.isPresent();

		if (!isOwner && !isGroupMember) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제에 대한 권한이 없습니다. : create // 해당 그룹의 멤버가 아닙니다.");
		} else if (!isOwner && !isAdmin) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제에 대한 권한이 없습니다. : create // 방장, 부방장일 경우에만 생성이 가능합니다.");
		}

		String number = getProblemId(request);
		int level = Integer.parseInt(getProblemLevel(number));
		String title = getProblemTitle(number);

		problemRepository.save(Problem.builder()
			.studyGroup(group)
			.link(request.link())
			.number(Integer.parseInt(number))
			.title(title)
			.level(level)
			.startDate(request.startDate())
			.endDate(request.endDate())
			.build());

		List<GroupMember> members = groupMemberRepository.findAllByStudyGroup(group);
		List<String> users = members.stream().map(member -> member.getUser().getEmail()).toList();
		try {
			notificationService.sendList(users, "새로운 과제가 등록되었습니다.", group, null);
		} catch (Exception e) {
			log.info("failed to send notification", e);
		}
		log.info("success to create problem");

	}

	@Transactional
	public void editProblem(User user, EditProblemRequest request) {
		Problem problem = getProblem(request.problemId());
		StudyGroup group = getGroup(problem.getStudyGroup().getId());
		Optional<GroupMember> groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group);

		Boolean isOwner = (group.getOwner().getId().equals(user.getId()) && groupMember.isEmpty());
		Boolean isAdmin = (!groupMember.isEmpty() && groupMember.get().getRole().equals(RoleOfGroupMember.ADMIN));
		Boolean isGroupMember = groupMember.isPresent();

		if (!isOwner && !isGroupMember) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제에 대한 권한이 없습니다. : edit // 해당 그룹의 멤버가 아닙니다.");
		} else if (!isOwner && !isAdmin) {
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
				"문제에 대한 권한이 없습니다. : edit // 방장, 부방장일 경우에만 생성이 가능합니다.");
		}
		problem.editProblemInfo(request.startDate(), request.endDate());
		log.info("success to edit problem deadline");
	}

	@Transactional(readOnly = true)
	public GetProblemListsResponse getProblemList(User user, Long groupId, Pageable pageable) {
		StudyGroup group = getGroup(groupId);
		if (!group.getOwner().getId().equals(user.getId()) && !groupMemberRepository.existsByUserAndStudyGroup(user,
			group)) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다.");
		}

		Page<Problem> problems = problemRepository.findAllByStudyGroup(group, pageable);

		List<GetProblemResponse> inProgressProblems = new ArrayList<>();
		List<GetProblemResponse> expiredProblems = new ArrayList<>();

		problems.forEach(problem -> {
			String title = problem.getTitle();
			Long problemId = problem.getId();
			String link = problem.getLink();
			LocalDate startDate = problem.getStartDate();
			LocalDate endDate = problem.getEndDate();
			Integer level = problem.getLevel();
			boolean solved = solutionRepository.existsByUserAndProblemAndResult(user, problem, "맞았습니다!!");
			Integer correctCount = solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(problemId);
			Integer submitMemberCount = solutionRepository.countDistinctUsersByProblemId(problemId);
			Integer groupMemberCount = groupMemberRepository.countMembersByStudyGroupId(groupId) + 1;
			Integer accuracy;
			Boolean inProgress;

			if (problem.getEndDate() == null || LocalDate.now().isAfter(problem.getEndDate())) {
				inProgress = false;
			} else {
				inProgress = true;
			}

			if (submitMemberCount == 0) {
				accuracy = 0;
			} else {
				Double tempCorrectCount = correctCount.doubleValue();
				Double tempSubmitMemberCount = submitMemberCount.doubleValue();
				Double tempAccuracy = ((tempCorrectCount / tempSubmitMemberCount) * 100);
				accuracy = tempAccuracy.intValue();
			}

			GetProblemResponse response = new GetProblemResponse(title, problemId, link, startDate, endDate, level,
				solved, submitMemberCount, groupMemberCount, accuracy, inProgress);

			if (inProgress) {
				inProgressProblems.add(response);
			} else {
				expiredProblems.add(response);
			}
		});

		return new GetProblemListsResponse(inProgressProblems, expiredProblems, problems.getNumber(),
			problems.getTotalPages(), problems.getTotalElements());
	}

	@Transactional
	public void deleteProblem(User user, Long problemId) {
		Problem problem = getProblem(problemId);
		StudyGroup group = getGroup(problem.getStudyGroup().getId());
		checkOwnerPermission(user, group, "delete");

		problemRepository.delete(problem);
		log.info("success to delete problem");
	}

	@Transactional(readOnly = true)
	public List<GetProblemResponse> getDeadlineReachedProblemList(User user, Long groupId) {
		StudyGroup group = getGroup(groupId);
		if (!group.getOwner().getId().equals(user.getId())
			&& !groupMemberRepository.existsByUserAndStudyGroup(user, group))
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다.");

		List<Problem> problems = problemRepository.findAllByStudyGroupAndEndDateBetween(group, LocalDate.now(),
			LocalDate.now().plusDays(1));
		problems.sort(Comparator.comparing(Problem::getEndDate));

		return problems.stream().map(problem -> {
			Long problemId = problem.getId();
			Integer correctCount = solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(problemId);
			Integer submitMemberCount = solutionRepository.countDistinctUsersByProblemId(problemId);
			Integer groupMemberCount = groupMemberRepository.countMembersByStudyGroupId(groupId) + 1;
			Integer accuracy;
			Boolean inProgress;

			if (problem.getEndDate() == null || LocalDate.now().isAfter(problem.getEndDate())) {
				inProgress = false;
			} else
				inProgress = true;
			if (submitMemberCount == 0) {
				accuracy = 0;
			} else {
				Double tempCorrectCount = correctCount.doubleValue();
				Double tempSubmitMemberCount = submitMemberCount.doubleValue();
				Double tempAccuracy = ((tempCorrectCount / tempSubmitMemberCount) * 100);
				accuracy = tempAccuracy.intValue();
			}
			return new GetProblemResponse(
				problem.getTitle(),
				problemId,
				problem.getLink(),
				problem.getStartDate(),
				problem.getEndDate(),
				problem.getLevel(),
				solutionRepository.existsByUserAndProblemAndResult(user, problem, "맞았습니다!!"),
				submitMemberCount,
				groupMemberCount,
				accuracy,
				inProgress);
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<GetProblemResponse> getQueuedProblemList(User user, Long groupId) {
		StudyGroup group = getGroup(groupId);
		Optional<GroupMember> groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group);

		Boolean isOwner = (group.getOwner().getId().equals(user.getId()) && groupMember.isEmpty());
		Boolean isAdmin = (!groupMember.isEmpty() && groupMember.get().getRole().equals(RoleOfGroupMember.ADMIN));
		Boolean isGroupMember = groupMember.isPresent();

		if (!isGroupMember && !isOwner) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다. : 그룹원이 아닙니다");
		}

		if (isGroupMember && !isAdmin) {
			throw new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다. : 부방장이 아닙니다");
		}

		List<Problem> problems = problemRepository.findAllByStudyGroupAndStartDateAfter(group, LocalDate.now());
		List<GetProblemResponse> responseList = new ArrayList<>();

		problems.forEach(problem -> {
			String title = problem.getTitle();
			Long problemId = problem.getId();
			String link = problem.getLink();
			LocalDate startDate = problem.getStartDate();
			LocalDate endDate = problem.getEndDate();
			Integer level = problem.getLevel();
			boolean solved = false;
			Integer submitMemberCount = 0;
			Integer groupMemberCount = groupMemberRepository.countMembersByStudyGroupId(groupId) + 1;
			Integer accuracy = 0;
			Boolean inProgress = false;

			GetProblemResponse response = new GetProblemResponse(title, problemId, link, startDate, endDate, level,
				solved, submitMemberCount, groupMemberCount, accuracy, inProgress);

			responseList.add(response);
		});

		return responseList;
	}

	private Problem getProblem(Long problemId) {
		return problemRepository.findById(problemId)
			.orElseThrow(() -> new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));
	}

	private StudyGroup getGroup(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
	}

	private String getProblemLevel(String problemId) {
		final RestTemplate restTemplate = new RestTemplate();

		String url = "https://solved.ac/api/v3/problem/lookup?problemIds=" + problemId;

		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String responseBody = responseEntity.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(responseBody);
			if (root.isArray() && root.size() > 0) {
				JsonNode firstElement = root.get(0);
				int level = firstElement.get("level").asInt();
				return String.valueOf(level);
			} else {
				System.out.println("No data found for the given problem ID");
				return "No data found";
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
			return "Error occurred";
		}
	}

	private String getProblemTitle(String problemId) {
		final RestTemplate restTemplate = new RestTemplate();
		String url = "https://solved.ac/api/v3/problem/lookup?problemIds=" + problemId;

		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String responseBody = responseEntity.getBody();

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(responseBody);
			if (root.isArray() && root.size() > 0) {
				JsonNode firstElement = root.get(0);
				return firstElement.get("titleKo").asText();
			} else {
				System.out.println("No data found for the given problem ID");
				return "No data found";
			}
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage());
			return "Error occurred";
		}
	}

	private String getProblemId(CreateProblemRequest request) {
		String url = request.link();
		String[] parts = url.split("/");
		if (!parts[2].equals("www.acmicpc.net"))
			throw new NotBojLinkException(HttpStatus.BAD_REQUEST.value(), "백준 링크가 아닙니다");
		return parts[parts.length - 1];
	}
}
