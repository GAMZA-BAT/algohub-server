package com.gamzabat.algohub.feature.notification.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingService {
	private final NotificationSettingRepository notificationSettingRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final StudyGroupRepository studyGroupRepository;

	@Transactional(readOnly = true)
	public List<GetNotificationSettingResponse> getNotificationSettings(User user) {
		List<NotificationSetting> settings = notificationSettingRepository.findAllByUser(user);
		return settings.stream().map(GetNotificationSettingResponse::toDTO).toList();
	}

	@Transactional
	public void editNotificationSettings(User user, EditNotificationSettingRequest request) {
		StudyGroup studyGroup = studyGroupRepository.findById(request.groupId())
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 스터디 그룹입니다."));
		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(() -> new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹입니다."));
		NotificationSetting setting = notificationSettingRepository.findByMember(member)
			.orElseThrow(() -> new CannotFoundNotificationSettingException("알림 설정 정보를 가져올 수 없습니다."));

		setting.editSettings(request.allNotifications(),
			request.newProblem(),
			request.newSolution(),
			request.newComment(),
			request.newMember(),
			request.deadlineReached()
		);

		log.info("success to edit notification settings");
	}
}
