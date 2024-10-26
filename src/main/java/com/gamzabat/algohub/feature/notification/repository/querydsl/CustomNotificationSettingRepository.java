package com.gamzabat.algohub.feature.notification.repository.querydsl;

import java.util.List;

import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.user.domain.User;

public interface CustomNotificationSettingRepository {
	List<NotificationSetting> findAllByUser(User user);
}
