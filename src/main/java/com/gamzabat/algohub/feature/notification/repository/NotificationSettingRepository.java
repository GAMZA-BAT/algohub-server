package com.gamzabat.algohub.feature.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.repository.querydsl.CustomNotificationSettingRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>,
	CustomNotificationSettingRepository {

}
