package com.gamzabat.algohub.feature.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.repository.querydsl.CustomNotificationSettingRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>,
	CustomNotificationSettingRepository {

	Optional<NotificationSetting> findByMember(GroupMember member);

	void deleteByMember(GroupMember member);
}
