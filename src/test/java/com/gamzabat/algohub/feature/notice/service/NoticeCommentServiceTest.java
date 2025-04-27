package com.gamzabat.algohub.feature.notice.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
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
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class NoticeCommentServiceTest {
	@InjectMocks
	private NoticeCommentService commentService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private NoticeCommentRepository commentRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private NoticeRepository noticeRepository;
	@Mock
	private NotificationRepository notificationRepository;
	private User user, user2;
	private NoticeComment comment, comment2;
	private Notice notice;
	private StudyGroup studyGroup;
	@Captor
	private ArgumentCaptor<NoticeComment> commentCaptor;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		studyGroup = StudyGroup.builder().build();
		notice = Notice.builder().author(user).studyGroup(studyGroup).content("notice content.").build();
		comment = NoticeComment.builder().user(user).content("content").notice(notice).build();
		comment2 = NoticeComment.builder().user(user2).content("content").notice(notice).build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);

		Field noticeField = Notice.class.getDeclaredField("id");
		noticeField.setAccessible(true);
		noticeField.set(notice, 10L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(studyGroup, 30L);

		Field commentField = Comment.class.getDeclaredField("id");
		commentField.setAccessible(true);
		commentField.set(comment, 40L);
		commentField.set(comment2, 41L);
	}

	@Test
	@DisplayName("댓글 작성 성공")
	void createComment_1() {

		CreateNoticeCommentRequest request = CreateNoticeCommentRequest.builder()
			.content("content")
			.build();
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.save(any(NoticeComment.class))).thenReturn(comment);

		// when
		commentService.createComment(user2, 10L, request);
		// then
		verify(commentRepository, times(1)).save(commentCaptor.capture());
		NoticeComment result = commentCaptor.getValue();
		assertThat(result.getContent()).isEqualTo("content");
		assertThat(result.getUser()).isEqualTo(user2);
		assertThat(result.getNotice()).isEqualTo(notice);
		// verify(notificationService, times(1)).send(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 공지")
	void createCommentFailed_1() {
		// given
		CreateNoticeCommentRequest request = CreateNoticeCommentRequest.builder()
			.content("content")
			.build();
		when(noticeRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user, 10L, request))
			.isInstanceOf(NoticeValidationException.class)
			.hasFieldOrPropertyWithValue("error", "공지사항이 존재하지 않습니다.");
		verify(notificationService, never()).send(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 그룹")
	void createCommentFailed_2() {
		// given
		CreateNoticeCommentRequest request = CreateNoticeCommentRequest.builder()
			.content("content")
			.build();
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user, 10L, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹이 존재하지 않습니다.");
		verify(notificationService, never()).send(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 참여하지 않은 그룹")
	void createCommentFailed_3() {
		// given
		CreateNoticeCommentRequest request = CreateNoticeCommentRequest.builder()
			.content("content")
			.build();
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user2, 10L, request))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
		verify(notificationService, never()).send(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 조회 성공")
	void getComment_1() {
		// given
		List<NoticeComment> list = new ArrayList<>(30);
		for (int i = 0; i < 30; i++)
			list.add(NoticeComment.builder()
				.notice(notice)
				.user(user)
				.content("content" + i)
				.build());
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.findAllByNotice(notice)).thenReturn(list);
		// when
		List<GetCommentResponse> result = commentService.getCommentList(user2, 10L);
		// then
		assertThat(result.size()).isEqualTo(30);
		for (int i = 0; i < 30; i++)
			assertThat(result.get(i).content()).isEqualTo("content" + (30 - i - 1));
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 공지")
	void getCommentListFailed_1() {
		// given
		when(noticeRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user, 10L))
			.isInstanceOf(NoticeValidationException.class)
			.hasFieldOrPropertyWithValue("error", "공지사항이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 그룹")
	void getCommentListFailed_2() {
		// given
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user, 10L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 참여하지 않은 그룹")
	void getCommentListFailed_3() {
		// given
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user2, 10L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 성공")
	void deleteComment_1() {
		// given
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.findById(41L)).thenReturn(Optional.ofNullable(comment2));
		// when
		commentService.deleteComment(user2, 41L);
		// then
		verify(commentRepository, times(1)).delete(comment2);
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 댓글")
	void deleteCommentFailed_1() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user, 40L))
			.isInstanceOf(CommentValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "댓글이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 댓글 삭제 권한 없음")
	void deleteCommentFailed_2() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user2, 40L))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "댓글 작성자만 삭제할 수 있습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 공지")
	void deleteCommentFailed_3() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user, 40L))
			.isInstanceOf(NoticeValidationException.class)
			.hasFieldOrPropertyWithValue("error", "공지사항이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 그룹")
	void deleteCommentFailed_4() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user, 40L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 참여하지 않은 그룹")
	void deleteCommentFailed_5() {
		// given
		when(commentRepository.findById(41L)).thenReturn(Optional.ofNullable(comment2));
		when(noticeRepository.findById(10L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user2, 41L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 수정 성공")
	void testUpdateCommentSuccess() {
		// given
		UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
		when(commentRepository.findById(40L)).thenReturn(Optional.of(comment));
		LocalDateTime previousUpdatedAt = comment.getUpdatedAt();

		// 현재 시간을 모킹
		LocalDateTime fixedNow = LocalDateTime.of(2024, 8, 23, 12, 0);
		try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
			mockedStatic.when(LocalDateTime::now).thenReturn(fixedNow);

			// when
			commentService.updateComment(user, 40L, request);

			// then
			verify(commentRepository).findById(anyLong());
			assertEquals("Updated content", comment.getContent());
			assertEquals(fixedNow, comment.getUpdatedAt());  // 모킹한 시간으로 검증

		}
	}

	@Test
	@DisplayName("댓글 수정 실패(작성자가 아님)")
	void testUpdateCommentFailed_1() {
		//given
		UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		//when,then
		assertThatThrownBy(() -> commentService.updateComment(user2, 40L, request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "댓글 작성자만 수정할 수 있습니다.");

	}

	@Test
	@DisplayName("댓글 수정 실패(존재하지 않는 댓글)")
	void testUpdateCommentFailed_2() {
		//given
		UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
		when(commentRepository.findById(50L)).thenReturn(Optional.empty());
		//when, then
		assertThatThrownBy(() -> commentService.updateComment(user2, 50L, request))
			.isInstanceOf(CommentValidationException.class)
			.extracting("code", "error")
			.containsExactly(HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다.");
	}
}
