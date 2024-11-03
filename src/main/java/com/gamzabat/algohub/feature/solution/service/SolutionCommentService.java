package com.gamzabat.algohub.feature.solution.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
import com.gamzabat.algohub.feature.comment.service.CommentService;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.domain.SolutionComment;
import com.gamzabat.algohub.feature.solution.dto.CreateSolutionCommentRequest;
import com.gamzabat.algohub.feature.solution.exception.SolutionValidationException;
import com.gamzabat.algohub.feature.solution.repository.SolutionCommentRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolutionCommentService implements CommentService<CreateSolutionCommentRequest> {
	private final SolutionCommentRepository commentRepository;
	private final SolutionRepository solutionRepository;
	private final ProblemRepository problemRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public void createComment(User user, CreateSolutionCommentRequest request) {
		Solution solution = checkSolutionValidation(user, request.solutionId());

		SolutionComment comment = commentRepository.save(SolutionComment.builder()
			.user(user)
			.solution(solution)
			.content(request.content())
			.createdAt(LocalDateTime.now())
			.build());

		sendCommentNotification(solution, user, request.content());
		log.info("success to create solution comment. commentId: {}, solutionId: {}", comment.getId(),
			solution.getId());
	}

	private void sendCommentNotification(Solution solution, User user, String content) {
		String message = content.length() <= 35 ? content : content.substring(0, 35) + "...";
		try {
			notificationService.send(solution.getUser().getEmail(),
				user.getNickname() + "님이 코멘트를 남겼습니다.",
				solution.getProblem().getStudyGroup(),
				message);
		} catch (Exception e) {
			log.info("failed to send solution comment notification. solutionId: {},  userId: {}, error: {}",
				solution.getId(), user.getId(), e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<GetCommentResponse> getCommentList(User user, Long solutionId) {
		Solution solution = checkSolutionValidation(user, solutionId);
		List<SolutionComment> list = commentRepository.findAllBySolution(solution);
		List<GetCommentResponse> result = list.stream().map(GetCommentResponse::toDTO).toList();
		log.info("success to get solution comment list. solutionId: {}", solutionId);
		return result;
	}

	@Override
	@Transactional
	public void updateComment(User user, UpdateCommentRequest request) {
		SolutionComment comment = commentRepository.findById(request.commentId())
			.orElseThrow(() -> new CommentValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 댓글 입니다."));
		if (!comment.getUser().getId().equals(user.getId()))
			throw new UserValidationException("댓글 작성자가 아닙니다.");

		comment.updateComment(request.content());
		log.info("success to update solution comment. commentId: {}", request.commentId());
	}

	@Override
	@Transactional
	public void deleteComment(User user, Long commentId) {
		SolutionComment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 댓글 입니다."));
		if (!comment.getUser().getId().equals(user.getId()))
			throw new CommentValidationException(HttpStatus.FORBIDDEN.value(), "댓글 삭제에 대한 권한이 없습니다.");

		checkSolutionValidation(user, comment.getSolution().getId());
		commentRepository.delete(comment);
		log.info("success to delete solution comment. commentId: {}", commentId);
	}

	private Solution checkSolutionValidation(User user, Long solutionId) {
		Solution solution = solutionRepository.findById(solutionId)
			.orElseThrow(() -> new SolutionValidationException("존재하지 않는 풀이 입니다."));

		Problem problem = problemRepository.findById(solution.getProblem().getId())
			.orElseThrow(() -> new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));

		StudyGroup group = studyGroupRepository.findById(problem.getStudyGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다.");

		return solution;
	}

}
