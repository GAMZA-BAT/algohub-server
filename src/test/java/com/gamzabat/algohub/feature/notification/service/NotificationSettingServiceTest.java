package com.gamzabat.algohub.feature.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.dto.EditNotificationSettingRequest;
import com.gamzabat.algohub.feature.notification.dto.GetNotificationSettingResponse;
import com.gamzabat.algohub.feature.notification.exception.CannotFoundNotificationSettingException;
import com.gamzabat.algohub.feature.notification.repository.NotificationSettingRepository;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {
	@InjectMocks
	private NotificationSettingService notificationSettingService;
	@Mock
	private NotificationSettingRepository notificationSettingRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	private User user, owner, user2, user3;
	private StudyGroup group;
	private GroupMember groupMember1;
	private final Long groupId = 10L;
	private NotificationSetting setting1;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image1").build();
		owner = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image1").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image2").build();
		user3 = User.builder().email("email3").password("password").nickname("nickname3")
			.role(Role.USER).profileImage("image3").build();

		group = StudyGroup.builder()
			.name("name")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.groupImage("imageUrl")
			.groupCode("code")
			.build();
		groupMember1 = GroupMember.builder()
			.studyGroup(group)
			.user(user)
			.role(RoleOfGroupMember.OWNER)
			.joinDate(LocalDate.now())
			.build();

		setting1 = new NotificationSetting(groupMember1);

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(owner, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group, 10L);
	}

	@Test
	@DisplayName("알림 설정 목록 조회")
	void getNotificationSettings() {
		// given
		when(notificationSettingRepository.findAllByUser(user)).thenReturn(List.of(setting1));
		// when
		List<GetNotificationSettingResponse> result = notificationSettingService.getNotificationSettings(
			user);
		// then
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.getFirst().allNotifications()).isTrue();
		assertThat(result.getFirst().groupName()).isEqualTo("name");
	}

	@Test
	@DisplayName("알림 설정 수정 성공")
	void editNotificationSettings() {
		// given
		EditNotificationSettingRequest request = new EditNotificationSettingRequest(groupId, true, false, false, false,
			false, true);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.of(groupMember1));
		when(notificationSettingRepository.findByMember(groupMember1)).thenReturn(Optional.ofNullable(setting1));
		// when
		notificationSettingService.editNotificationSettings(user, request);
		// then
		assertThat(setting1.isNewProblem()).isFalse();
		assertThat(setting1.isNewComment()).isFalse();
		assertThat(setting1.isNewSolution()).isFalse();
		assertThat(setting1.isNewMember()).isFalse();
		assertThat(setting1.isDeadlineReached()).isTrue();
	}

	@Test
	@DisplayName("알림 설정 수정 실패 : 존재하지 않는 그룹")
	void editNotificationSettingsFailed_1() {
		// given
		EditNotificationSettingRequest request = new EditNotificationSettingRequest(groupId, true, false, false, false,
			false, true);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> notificationSettingService.editNotificationSettings(user, request))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "존재하지 않는 스터디 그룹입니다.");
	}

	@Test
	@DisplayName("알림 설정 수정 실패 : 참여하지 않은 그룹")
	void editNotificationSettingsFailed_2() {
		// given
		EditNotificationSettingRequest request = new EditNotificationSettingRequest(groupId, true, false, false, false,
			false, true);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> notificationSettingService.editNotificationSettings(user, request))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 스터디 그룹입니다.");
	}

	@Test
	@DisplayName("알림 설정 수정 실패 : 알림 설정 정보 가져오기 실패")
	void editNotificationSettingsFailed_3() {
		// given
		EditNotificationSettingRequest request = new EditNotificationSettingRequest(groupId, true, false, false, false,
			false, true);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.of(groupMember1));
		when(notificationSettingRepository.findByMember(groupMember1)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> notificationSettingService.editNotificationSettings(user, request))
			.isInstanceOf(CannotFoundNotificationSettingException.class)
			.hasFieldOrPropertyWithValue("error", "알림 설정 정보를 가져올 수 없습니다.");
	}
}