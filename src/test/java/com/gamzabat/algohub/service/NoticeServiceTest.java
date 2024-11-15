package com.gamzabat.algohub.service;

import static com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember.*;
import static org.assertj.core.api.Assertions.*;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notice.domain.Notice;
import com.gamzabat.algohub.feature.notice.domain.NoticeRead;
import com.gamzabat.algohub.feature.notice.dto.CreateNoticeRequest;
import com.gamzabat.algohub.feature.notice.dto.GetNoticeResponse;
import com.gamzabat.algohub.feature.notice.dto.UpdateNoticeRequest;
import com.gamzabat.algohub.feature.notice.exception.NoticeValidationException;
import com.gamzabat.algohub.feature.notice.repository.NoticeCommentRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeReadRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeRepository;
import com.gamzabat.algohub.feature.notice.service.NoticeService;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
public class NoticeServiceTest {
	@InjectMocks
	private NoticeService noticeService;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private NoticeRepository noticeRepository;
	@Mock
	GroupMemberRepository groupMemberRepository;
	@Mock
	private NoticeCommentRepository noticeCommentRepository;
	@Mock
	private NoticeReadRepository noticeReadRepository;
	@Captor
	private ArgumentCaptor<Notice> noticeCaptor;

	private User user, user2, user3, user4;
	private StudyGroup studyGroup;
	private GroupMember groupMember2, groupMember3;
	private Notice notice;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		user3 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		user4 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		studyGroup = StudyGroup.builder().build();
		groupMember2 = GroupMember.builder().user(user2).studyGroup(studyGroup).role(ADMIN).build();
		groupMember3 = GroupMember.builder().user(user3).studyGroup(studyGroup).role(PARTICIPANT).build();
		notice = Notice.builder()
			.studyGroup(studyGroup)
			.createdAt(LocalDateTime.now())
			.title("title")
			.content("content")
			.category("category")
			.author(user)
			.build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);
		userField.set(user4, 4L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(studyGroup, 30L);

		Field groupMemberField = GroupMember.class.getDeclaredField("id");
		groupMemberField.setAccessible(true);
		groupMemberField.set(groupMember2, 200L);
		groupMemberField.set(groupMember3, 300L);

		Field noticeField = Notice.class.getDeclaredField("id");
		noticeField.setAccessible(true);
		noticeField.set(notice, 1000L);

	}

	@Test
	@DisplayName("공지 작성 성공")
	void createNoticeSuccess_1() {
		//given
		CreateNoticeRequest request = new CreateNoticeRequest("title", "content", "category");
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, studyGroup)).thenReturn(
			Optional.ofNullable(groupMember2));
		//when
		noticeService.createNotice(user2, 30L, request);
		//then
		verify(noticeRepository, times(1)).save(noticeCaptor.capture());
		Notice result = noticeCaptor.getValue();
		assertThat(result.getAuthor()).isEqualTo(user2);
		assertThat(result.getContent()).isEqualTo("content");
		assertThat(result.getTitle()).isEqualTo("title");
		assertThat(result.getCategory()).isEqualTo("category");
		assertThat(result.getStudyGroup()).isEqualTo(studyGroup);

	}

	@Test
	@DisplayName("공지 작성 실패 그룹장or부방장이 아님")
	void createNoticeFail_1() {
		//given
		CreateNoticeRequest request = new CreateNoticeRequest("title", "content", "category");
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.findByUserAndStudyGroup(user3, studyGroup)).thenReturn(
			Optional.ofNullable(groupMember3));
		//when,then
		assertThatThrownBy(() -> noticeService.createNotice(user3, 30L, request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "공지 작성 권한이 없습니다");

	}

	@Test
	@DisplayName("공지 작성 실패 존재하지 않는 그룹")
	void createNoticeFail_2() {
		//given
		CreateNoticeRequest request = new CreateNoticeRequest("title", "content", "category");
		when(studyGroupRepository.findById(31L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.createNotice(user, 31L, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.extracting("code", "error")
			.containsExactly(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다");
	}

	@Test
	@DisplayName("공지 작성 실패 존재하지 않는 멤버")
	void createNoticeFail_3() {
		//given
		CreateNoticeRequest request = new CreateNoticeRequest("title", "content", "category");
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.findByUserAndStudyGroup(user4, studyGroup)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.createNotice(user4, 30L, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("공지 조회 성공")
	void getNoticeSuccess_1() {
		//given
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(
			true);
		//when
		GetNoticeResponse response = noticeService.getNotice(user2, 1000L);
		//then
		assertThat(response.author()).isEqualTo("nickname1");
		assertThat(response.content()).isEqualTo("content");
		assertThat(response.title()).isEqualTo("title");
		assertThat(response.category()).isEqualTo("category");
		assertThat(response.createAt()).isEqualTo(DateFormatUtil.formatDateTimeForNotice(notice.getCreatedAt()));
		assertThat(response.noticeId()).isEqualTo(1000L);
		verify(noticeReadRepository, times(1)).save(any(NoticeRead.class));
	}

	@Test
	@DisplayName("공지 조회 실패(존재하지 않는 공지)")
	void getNoticeFailed_1() {
		//given
		when(noticeRepository.findById(1001L)).thenReturn(Optional.empty());

		//when, then
		assertThatThrownBy(() -> noticeService.getNotice(user, 1001L))
			.isInstanceOf(NoticeValidationException.class)
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 게시글입니다");

	}

	@Test
	@DisplayName("공지 조회 실패(그룹에 참여하지 않은 유저)")
	void getNoticeFailed_2() {
		//given
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(groupMemberRepository.existsByUserAndStudyGroup(user4, notice.getStudyGroup())).thenReturn(false);
		//when
		assertThatThrownBy(() -> noticeService.getNotice(user4, 1000L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 스터디 그룹 입니다.");
	}

	@Test
	@DisplayName("공지 목록 조회 성공")
	void getNoticeListSuccess_1() {
		//given
		List<Notice> noticeList = new ArrayList<>(10);
		for (int i = 0; i < 10; i++)
			noticeList.add(
				Notice.builder()
					.author(user)
					.content("content" + i)
					.title("title" + i)
					.category("category" + i)
					.createdAt(LocalDateTime.now())
					.studyGroup(studyGroup)
					.build());
		for (int i = 10; i < 20; i++)
			noticeList.add(
				Notice.builder()
					.author(user2)
					.content("content" + i)
					.title("title" + i)
					.category("category" + i)
					.createdAt(LocalDateTime.now())
					.studyGroup(studyGroup)
					.build());
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup)).thenReturn(true);
		when(noticeRepository.findAllByStudyGroup(studyGroup)).thenReturn(noticeList);
		//when
		List<GetNoticeResponse> result = noticeService.getNoticeList(user, 30L);
		//then
		assertThat(result.size()).isEqualTo(20);
		for (int i = 0; i < 20; i++) {
			assertThat(result.get(i).content()).isEqualTo("content" + i);
			assertThat(result.get(i).title()).isEqualTo("title" + i);
			assertThat(result.get(i).category()).isEqualTo("category" + i);
			assertThat(result.get(i).isRead()).isFalse();
		}
	}

	@Test
	@DisplayName("공지 목록 조회 실패 (존재하지 않는 스터디 그룹임)")
	void getNoticeListFailed_1() {
		//given
		when(studyGroupRepository.findById(31L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.getNoticeList(user, 31L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 스터디 그룹입니다");
	}

	@Test
	@DisplayName("공지 목록 조회 실패(참여하지 않은 스터디 그룹)")
	void getNoticeListFailed_2() {
		//given
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user4, studyGroup)).thenReturn(false);
		//when, then
		assertThatThrownBy(() -> noticeService.getNoticeList(user4, 30L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 스터디 그룹입니다");
	}

	@Test
	@DisplayName("공지 수정 성공")
	void updateNoticeSuccess() {
		//given
		UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("updateTitle", "updateContent",
			"updateCategory");
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		//when
		noticeService.updateNotice(user, 1000L, updateNoticeRequest);
		//then
		assertThat(notice.getContent()).isEqualTo("updateContent");
		assertThat(notice.getTitle()).isEqualTo("updateTitle");
		assertThat(notice.getCategory()).isEqualTo("updateCategory");
	}

	@Test
	@DisplayName("공지 수정 실패(존재하지 않는 게시글)")
	void updateNoticeFailed_1() {
		//given
		UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("updateTitle", "updateContent",
			"updateCategory");
		when(noticeRepository.findById(1001L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.updateNotice(user, 1001L, updateNoticeRequest))
			.isInstanceOf(NoticeValidationException.class)
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 게시글입니다");
	}

	@Test
	@DisplayName("공시 수정 실패(존재하지 않는 스터디 그룹)")
	void updateNoticeFailed_2() {
		//given
		UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("updateTitle", "updateContent",
			"updateCategory");
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.updateNotice(user, 1000L, updateNoticeRequest))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 스터디 그룹입니다");

	}

	@Test
	@DisplayName("공지 수정 실패(게시글 작성자가 아님)")
	void updateNoticeFailed_3() {
		//given
		UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("updateTitle", "updateContent",
			"updateCategory");
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		//when, then
		assertThatThrownBy(() -> noticeService.updateNotice(user4, 1000L, updateNoticeRequest))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "공지를 수정할 수 있는 권한이 없습니다");
	}

	@Test
	@DisplayName("공지 삭제 성공")
	void deleteNoticeSuccess() {
		//given
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		doNothing().when(noticeCommentRepository).deleteAllCommentByNotice(notice);
		//when
		noticeService.deleteNotice(user, 1000L);
		//then
		verify(noticeRepository, times(1)).delete(notice);
	}

	@Test
	@DisplayName("공지 삭제 실패(존재하지 않는 게시글)")
	void deleteNoticeFailed_1() {
		//given
		when(noticeRepository.findById(1001L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.deleteNotice(user, 1001L))
			.isInstanceOf(NoticeValidationException.class)
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 게시글입니다");
	}

	@Test
	@DisplayName("공지 삭제 실패(존재하지 않는 스터디 그룹)")
	void deleteNoticeFailed_2() {
		//given
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> noticeService.deleteNotice(user, 1000L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 스터디 그룹입니다");
	}

	@Test
	@DisplayName("공지 삭제 실패(게시글 작성자가 아님)")
	void deleteNoticeFailed_3() {
		//given
		when(noticeRepository.findById(1000L)).thenReturn(Optional.ofNullable(notice));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		//when, then
		assertThatThrownBy(() -> noticeService.deleteNotice(user4, 1000L))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "공지를 삭제할 수 있는 권한이 없습니다");
	}

}
