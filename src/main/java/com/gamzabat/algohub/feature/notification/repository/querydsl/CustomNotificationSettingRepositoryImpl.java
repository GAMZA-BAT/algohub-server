package com.gamzabat.algohub.feature.notification.repository.querydsl;

import static com.gamzabat.algohub.feature.notification.domain.QNotificationSetting.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomNotificationSettingRepositoryImpl implements CustomNotificationSettingRepository {
	private final JPAQueryFactory query;

	@Override
	public List<NotificationSetting> findAllByUser(User user) {
		return query.selectFrom(notificationSetting)
			.where(notificationSetting.member.user.eq(user))
			.fetch();
	}

	@Override
	public List<NotificationSetting> findAllByStudyGroup(StudyGroup studyGroup) {
		return query.selectFrom(notificationSetting)
			.where(notificationSetting.member.studyGroup.eq(studyGroup))
			.fetch();
	}
}
