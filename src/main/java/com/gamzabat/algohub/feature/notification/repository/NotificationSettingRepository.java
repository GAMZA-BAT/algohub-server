package com.gamzabat.algohub.feature.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.repository.querydsl.CustomNotificationSettingRepository;
import com.gamzabat.algohub.feature.user.domain.User;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>,
	CustomNotificationSettingRepository {

	Optional<NotificationSetting> findByMember(GroupMember member);

	Optional<NotificationSetting> findByUserAndGroup(User user, StudyGroup studyGroup);
}
