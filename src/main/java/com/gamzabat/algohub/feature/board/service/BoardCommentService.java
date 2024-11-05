package com.gamzabat.algohub.feature.board.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.domain.BoardComment;
import com.gamzabat.algohub.feature.board.dto.CreateBoardCommentRequest;
import com.gamzabat.algohub.feature.board.exception.BoardValidationExceoption;
import com.gamzabat.algohub.feature.board.repository.BoardCommentRepository;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
import com.gamzabat.algohub.feature.comment.service.CommentService;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardCommentService implements CommentService<CreateBoardCommentRequest> {
	private final BoardRepository boardRepository;
	private final BoardCommentRepository boardCommentRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public void createComment(User user, CreateBoardCommentRequest request) {
		Board board = validateBoard(user, request.boardId());

		BoardComment comment = boardCommentRepository.save(BoardComment.builder()
			.user(user)
			.board(board)
			.content(request.content())
			.build());

		sendCommentNotification(board, user, request.content());
		log.info("success to create board comment. commentId: {}, boardId: {}", comment.getId(), board.getId());
	}

	private void sendCommentNotification(Board board, User user, String content) {
		String message = content.length() <= 35 ? content : content.substring(0, 35) + "...";
		try {
			notificationService.send(board.getAuthor().getEmail(),
				user.getNickname() + "님이 코멘트를 남겼습니다.",
				board.getStudyGroup(),
				message);
		} catch (Exception e) {
			log.info("failed to send comment board notification. boardId: {}, userId: {}, error: {}",
				board.getId(), user.getId(), e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<GetCommentResponse> getCommentList(User user, Long boardId) {
		Board board = validateBoard(user, boardId);
		List<BoardComment> boards = boardCommentRepository.findAllByBoard(board);
		log.info("success to get board comment list. boardId: {}", boardId);
		return boards.stream().map(GetCommentResponse::toDTO).toList();
	}

	@Override
	@Transactional
	public void updateComment(User user, UpdateCommentRequest request) {
		BoardComment comment = boardCommentRepository.findById(request.commentId())
			.orElseThrow(() -> new CommentValidationException(
				HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다."));
		if (!comment.getUser().getId().equals(user.getId()))
			throw new UserValidationException("댓글 작성자만 수정할 수 있습니다.");

		comment.updateComment(request.content());
		log.info("success to update board comment. commentId: {}", comment.getId());

	}

	@Override
	@Transactional
	public void deleteComment(User user, Long commentId) {
		BoardComment comment = boardCommentRepository.findById(commentId)
			.orElseThrow(() -> new CommentValidationException(
				HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다."));
		if (!comment.getUser().getId().equals(user.getId()))
			throw new UserValidationException("댓글 작성자만 삭제할 수 있습니다.");

		validateBoard(user, comment.getBoard().getId());
		boardCommentRepository.delete(comment);
		log.info("success to delete board comment. commentId: {}", commentId);

	}

	private Board validateBoard(User user, Long boardId) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new BoardValidationExceoption("공지사항이 존재하지 않습니다."));

		StudyGroup group = studyGroupRepository.findById(board.getStudyGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(
				HttpStatus.NOT_FOUND.value(), "스터디 그룹이 존재하지 않습니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다.");

		return board;
	}
}
