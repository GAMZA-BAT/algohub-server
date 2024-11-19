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
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notification.domain.Notification;
import com.gamzabat.algohub.feature.notification.exception.CannotFoundNotificationException;
import com.gamzabat.algohub.feature.notification.exception.NotificationValidationException;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
	@InjectMocks
	private NotificationService notificationService;
	@Mock
	private NotificationRepository notificationRepository;
	private User user, user2;
	private StudyGroup group;
	private Notification notification1, notification2;
	private final Long notificationId = 10L;
	private final Long userId = 1L;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image1").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image2").build();
		group = StudyGroup.builder()
			.name("name")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.groupImage("imageUrl")
			.groupCode("code")
			.build();
		notification1 = Notification.builder().isRead(false).studyGroup(group).user(user).message("message1").build();
		notification2 = Notification.builder().isRead(false).studyGroup(group).user(user).message("message2").build();

		Field userId = User.class.getDeclaredField("id");
		userId.setAccessible(true);
		userId.set(user, 1L);

		Field notificationId = Notification.class.getDeclaredField("id");
		notificationId.setAccessible(true);
		notificationId.set(notification1, 10L);
		notificationId.set(notification2, 20L);
	}

	@Test
	@DisplayName("전체 알림 읽음 표시 성공")
	void readAllNotifications() {
		// given
		when(notificationRepository.findAllByUserAndIsRead(user, false)).thenReturn(
			List.of(notification1, notification2));
		// when
		notificationService.readAllNotifications(user);
		// then
		assertThat(notification1.isRead()).isTrue();
		assertThat(notification2.isRead()).isTrue();
	}

	@Test
	@DisplayName("알림 단건 읽음 표시 성공")
	void readNotification() {
		// given
		when(notificationRepository.findById(notificationId)).thenReturn(Optional.ofNullable(notification1));
		// when
		notificationService.readNotification(user, notificationId);
		// then
		assertThat(notification1.isRead()).isTrue();
	}

	@Test
	@DisplayName("알림 단건 읽음 표시 실패 : 존재하지 않는 알림")
	void readNotificationFailed_1() {
		// given
		when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> notificationService.readNotification(user, notificationId))
			.isInstanceOf(CannotFoundNotificationException.class)
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 알림입니다.");
	}

	@Test
	@DisplayName("알림 단건 읽음 표시 실패 : 알림 주인 불일치")
	void readNotificationFailed_2() {
		// given
		when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification1));
		// when, then
		assertThatThrownBy(() -> notificationService.readNotification(user2, notificationId))
			.isInstanceOf(NotificationValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "알림의 주인이 일치하지 않습니다.");
	}
}