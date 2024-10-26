package com.gamzabat.algohub.feature.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.dto.GetNotificationSettingResponse;
import com.gamzabat.algohub.feature.notification.repository.NotificationSettingRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingService {
	private final NotificationSettingRepository notificationSettingRepository;

	@Transactional(readOnly = true)
	public List<GetNotificationSettingResponse> getNotificationSettings(User user) {
		List<NotificationSetting> settings = notificationSettingRepository.findAllByUser(user);
		return settings.stream().map(GetNotificationSettingResponse::toDTO).toList();
	}
}
