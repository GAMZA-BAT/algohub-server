package com.gamzabat.algohub.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.gamzabat.algohub.domain.Problem;
import com.gamzabat.algohub.domain.StudyGroup;
import com.gamzabat.algohub.domain.User;
import com.gamzabat.algohub.dto.CreateProblemRequest;
import com.gamzabat.algohub.dto.EditProblemRequest;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.repository.ProblemRepository;
import com.gamzabat.algohub.repository.StudyGroupRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProblemService {
	private final ProblemRepository problemRepository;
	private final StudyGroupRepository studyGroupRepository;
	public void createProblem(User user, CreateProblemRequest request) {
		StudyGroup group = getGroup(request.groupId());

		checkOwnerPermission(user, group, "create");

		problemRepository.save(Problem.builder() // 크롤링 후 level, title 정보 필요
			.studyGroup(group)
			.link(request.link())
			.deadline(request.deadline())
			.build());

		log.info("success to create problem");
	}

	public void editProblem(User user, EditProblemRequest request) {
		Problem problem = problemRepository.findById(request.problemId())
			.orElseThrow(() -> new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));
		StudyGroup group = getGroup(problem.getStudyGroup().getId());
		checkOwnerPermission(user, group, "edit");

		problem.editDeadline(request.deadline());
		log.info("success to edit problem deadline");
	}

	private StudyGroup getGroup(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
	}

	private static void checkOwnerPermission(User user, StudyGroup group, String permission) {
		if(!group.getOwner().getId().equals(user.getId()))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "문제에 대한 권한이 없습니다. : "+permission);
	}
}
