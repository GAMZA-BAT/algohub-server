package com.gamzabat.algohub.feature.notice.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notice.domain.Notice;
import com.gamzabat.algohub.feature.notice.domain.NoticeRead;
import com.gamzabat.algohub.feature.notice.dto.CreateNoticeRequest;
import com.gamzabat.algohub.feature.notice.dto.CreateNoticeResponse;
import com.gamzabat.algohub.feature.notice.dto.GetNoticeResponse;
import com.gamzabat.algohub.feature.notice.dto.UpdateNoticeRequest;
import com.gamzabat.algohub.feature.notice.exception.NoticeValidationException;
import com.gamzabat.algohub.feature.notice.repository.NoticeCommentRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeReadRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor

public class NoticeService {
	private final NoticeRepository noticeRepository;
	private final NoticeCommentRepository noticeCommentRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final NoticeReadRepository noticeReadRepository;

	@Transactional
	public CreateNoticeResponse createNotice(@AuthedUser User user, Long groupId, CreateNoticeRequest request) {
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다"));
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(
				() -> new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isParticipant(groupMember))
			throw new UserValidationException("공지 작성 권한이 없습니다");

		Notice notice = noticeRepository.save(Notice.builder()
			.author(user)
			.studyGroup(studyGroup)
			.title(request.title())
			.content(request.content())
			.category(request.category())
			.createdAt(LocalDateTime.now())
			.build());
		log.info("success to create notice");
		return new CreateNoticeResponse(notice.getId());
	}

	@Transactional(readOnly = false)
	public GetNoticeResponse getNotice(@AuthedUser User user, Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new NoticeValidationException("존재하지 않는 게시글입니다"));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, notice.getStudyGroup()))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹 입니다.");

		log.info("success to get notice");
		return GetNoticeResponse.builder()
			.author(notice.getAuthor().getNickname())
			.authorImage(notice.getAuthor().getProfileImage())
			.noticeId(notice.getId())
			.title(notice.getTitle())
			.content(notice.getContent())
			.category(notice.getCategory())
			.createdAt(DateFormatUtil.formatDateTimeForNotice(notice.getCreatedAt()))
			.isRead(true)
			.build();
	}

	@Transactional
	public void markNoticeAsRead(@AuthedUser User user, Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new NoticeValidationException("존재하지 않는 게시글입니다"));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, notice.getStudyGroup()))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹 입니다.");

		if (!noticeReadRepository.existsByNoticeAndUser(notice, user)) {
			noticeReadRepository.save(
				NoticeRead.builder().notice(notice).user(user).build()
			);
		}
		log.info("success to read notice. userId: {}, noticeId: {}", user.getId(), notice.getId());
	}

	@Transactional(readOnly = true)
	public Page<GetNoticeResponse> getNoticeList(@AuthedUser User user, Long studyGroupId, Pageable pageable) {
		StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다"));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹입니다");

		Page<Notice> list = noticeRepository.findAllByStudyGroup(studyGroup, pageable);
		Page<GetNoticeResponse> responses = list.map(
			notice -> GetNoticeResponse.toDTO(notice, noticeReadRepository.existsByNoticeAndUser(notice, user)));
		log.info("success to get notice list");
		return responses;
	}

	@Transactional
	public void updateNotice(User user, Long noticeId, UpdateNoticeRequest request) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new NoticeValidationException("존재하지 않는 게시글입니다"));
		validateStudyGroupExists(notice);
		if (!user.getId().equals(notice.getAuthor().getId()))
			throw new UserValidationException("공지를 수정할 수 있는 권한이 없습니다");
		if (request.title() != null) {
			notice.updateTitle(request.title());
		}
		if (request.content() != null) {
			notice.updateContent(request.content());
		}
		if (request.category() != null) {
			notice.updateCategory(request.category());
		}
		log.info("success to update notice");
	}

	@Transactional
	public void deleteNotice(User user, Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new NoticeValidationException("존재하지 않는 게시글입니다"));
		validateStudyGroupExists(notice);

		if (!user.getId().equals(notice.getAuthor().getId()))
			throw new UserValidationException("공지를 삭제할 수 있는 권한이 없습니다");

		noticeCommentRepository.deleteAllCommentByNotice(notice);
		noticeReadRepository.deleteAllByNotice(notice);
		noticeRepository.delete(notice);

		log.info("success to delete notice. userId: {}, noticeId: {}", user.getId(), noticeId);
	}

	private void validateStudyGroupExists(Notice notice) {
		studyGroupRepository.findById(notice.getStudyGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다"));
	}

}
