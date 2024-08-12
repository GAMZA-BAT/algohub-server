package com.gamzabat.algohub.feature.comment.service;

import java.time.LocalDateTime;
import java.util.List;

import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
import com.gamzabat.algohub.feature.comment.exception.SolutionValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.comment.repository.CommentRepository;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final SolutionRepository solutionRepository;
	private final ProblemRepository problemRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final NotificationService notificationService;

	@Transactional
	public void createComment(User user, CreateCommentRequest request) {
		Solution solution = checkSolutionValidation(user, request.solutionId());

		commentRepository.save(Comment.builder()
				.user(user)
				.solution(solution)
				.content(request.content())
				.createdAt(LocalDateTime.now())
			.build());

		String message;
		if(request.content().length()<35)
			message = request.content();
		else
			message = request.content().substring(0,35)+"...";

		try {
			notificationService.send(solution.getUser().getEmail(),
					user.getNickname() + "님이 코멘트를 남겼습니다.",
					solution.getProblem().getStudyGroup(),
					message);
		}catch (Exception e) {
			log.info("failed to send comment notification", e);
		}
		log.info("success to create comment");
	}

	@Transactional(readOnly = true)
	public List<GetCommentResponse> getCommentList(User user, Long solutionId) {
		Solution solution = checkSolutionValidation(user, solutionId);
		List<Comment> list = commentRepository.findAllBySolution(solution);
		List<GetCommentResponse> result = list.stream().map(GetCommentResponse::toDTO).toList();
		log.info("success to get comment list");
		return result;
	}

	@Transactional
	public void deleteComment(User user, Long commentId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 댓글 입니다."));
		if(!comment.getUser().getId().equals(user.getId()))
			throw new CommentValidationException(HttpStatus.FORBIDDEN.value(),"댓글 삭제에 대한 권한이 없습니다.");

		checkSolutionValidation(user, comment.getSolution().getId());
		commentRepository.delete(comment);
		log.info("success to delete comment");
	}

	private Solution checkSolutionValidation(User user, Long solutionId) {
		Solution solution = solutionRepository.findById(solutionId)
			.orElseThrow(() -> new SolutionValidationException("존재하지 않는 풀이 입니다."));

		Problem problem = problemRepository.findById(solution.getProblem().getId())
			.orElseThrow(() -> new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));

		StudyGroup group = studyGroupRepository.findById(problem.getStudyGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		if(!groupMemberRepository.existsByUserAndStudyGroup(user, group)
			&& !group.getOwner().getId().equals(user.getId()))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(),"참여하지 않은 그룹 입니다.");

		return solution;
	}
}
