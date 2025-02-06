package com.gamzabat.algohub.feature.notice.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.gamzabat.algohub.feature.notice.domain.Notice;
import com.gamzabat.algohub.feature.notice.domain.NoticeComment;
import com.gamzabat.algohub.feature.notice.dto.CreateNoticeCommentRequest;
import com.gamzabat.algohub.feature.notice.exception.NoticeValidationException;
import com.gamzabat.algohub.feature.notice.repository.NoticeCommentRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeCommentService implements CommentService<CreateNoticeCommentRequest> {
	private final NoticeRepository noticeRepository;
	private final NoticeCommentRepository noticeCommentRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;

	@Override
	@Transactional
	public void createComment(User user, Long noticeId, CreateNoticeCommentRequest request) {
		Notice notice = validateNotice(user, noticeId);

		NoticeComment comment = noticeCommentRepository.save(NoticeComment.builder()
			.user(user)
			.notice(notice)
			.content(request.content())
			.build());

		log.info("success to create notice comment. commentId: {}, noticeId: {}", comment.getId(), notice.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<GetCommentResponse> getCommentList(User user, Long noticeId) {
		Notice notice = validateNotice(user, noticeId);
		List<NoticeComment> notices = noticeCommentRepository.findAllByNotice(notice);
		log.info("success to get notice comment list. noticeId: {}", noticeId);
		return notices.stream().map(GetCommentResponse::toDTO).sorted(Comparator.comparing(
			GetCommentResponse::createdAt).reversed()).toList();
	}

	@Override
	@Transactional
	public void updateComment(User user, Long commentId, UpdateCommentRequest request) {
		NoticeComment comment = noticeCommentRepository.findById(commentId)
			.orElseThrow(() -> new CommentValidationException(
				HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다."));
		if (!comment.getUser().getId().equals(user.getId()))
			throw new UserValidationException("댓글 작성자만 수정할 수 있습니다.");

		if(request.content()!=null){
			comment.updateComment(request.content());
		}
		log.info("success to update notice comment. commentId: {}", comment.getId());

	}

	@Override
	@Transactional
	public void deleteComment(User user, Long commentId) {
		NoticeComment comment = noticeCommentRepository.findById(commentId)
			.orElseThrow(() -> new CommentValidationException(
				HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다."));
		if (!comment.getUser().getId().equals(user.getId()))
			throw new UserValidationException("댓글 작성자만 삭제할 수 있습니다.");

		validateNotice(user, comment.getNotice().getId());
		noticeCommentRepository.delete(comment);
		log.info("success to delete notice comment. commentId: {}", commentId);

	}

	private Notice validateNotice(User user, Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new NoticeValidationException("공지사항이 존재하지 않습니다."));

		StudyGroup group = studyGroupRepository.findById(notice.getStudyGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(
				HttpStatus.NOT_FOUND.value(), "스터디 그룹이 존재하지 않습니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다.");

		return notice;
	}
}
