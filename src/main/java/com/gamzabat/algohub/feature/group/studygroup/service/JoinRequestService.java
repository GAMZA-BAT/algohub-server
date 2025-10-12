package com.gamzabat.algohub.feature.group.studygroup.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.enums.JoinRequestStatus;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.JoinRequest;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.dto.UpdateJoinRequestStatusRequest;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.exception.JoinRequestException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.JoinRequestRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.repository.NotificationSettingRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestService {
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final JoinRequestRepository joinRequestRepository;
	private final NotificationSettingRepository notificationSettingRepository;
	private final RankingRepository rankingRepository;
	private final StudyGroupService studyGroupService;

	@Transactional
	public void joinRequest(User user, Long groupId) {
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
		if (groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup)) {
			throw new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "이미 가입한 그룹입니다");
		}
		if (joinRequestRepository.existsByGroup_IdAndRequester_Id(groupId, user.getId())) {
			throw new JoinRequestException("이미 요청한 그룹입니다.");
		}

		JoinRequest request = new JoinRequest(studyGroup, user);
		joinRequestRepository.save(request);
		log.info("success to join request group = {}", groupId);
	}

	@Transactional(readOnly = true)
	public List<JoinRequest> getAllJoinRequests(User user, Long groupId) {
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
		Optional<GroupMember> groupMember = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup);
		if (groupMember.isPresent() && RoleOfGroupMember.isParticipant(groupMember.get()) || groupMember.isEmpty()) {
			throw new JoinRequestException("요청 목록을 조회할 권한이 없습니다.");
		}
		return joinRequestRepository.findAllByGroup_Id(groupId);
	}

	@Transactional
	public void updateJoinRequest(User user, Long requestId, UpdateJoinRequestStatusRequest request) {
		JoinRequest joinRequest = joinRequestRepository.findById(requestId)
			.orElseThrow(() -> new JoinRequestException("해당 요청이 존재하지 않습니다"));
		StudyGroup studyGroup = studyGroupRepository.findById(joinRequest.getGroup().getId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹입니다."));
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(() -> new GroupMemberValidationException(HttpStatus.NOT_FOUND.value(), "해당 그룹의 멤버가 아닙니다."));
		if (RoleOfGroupMember.isParticipant(groupMember)) {
			throw new JoinRequestException("승인 권한이 없습니다.");
		}
		if (request.status() == JoinRequestStatus.APPROVE) {
			joinRequest.updateStatus(request.status());
			GroupMember newGroupMember = GroupMember.builder()
				.user(joinRequest.getRequester())
				.studyGroup(studyGroup)
				.joinDate(LocalDate.now())
				.role(RoleOfGroupMember.PARTICIPANT)
				.build();
			groupMemberRepository.save(newGroupMember);

			notificationSettingRepository.save(
				NotificationSetting.builder().member(newGroupMember).build()
			);

			rankingRepository.save(Ranking.builder()
				.member(newGroupMember)
				.currentRank(groupMemberRepository.countByStudyGroup(studyGroup))
				.solvedCount(0)
				.rankDiff("-")
				.build()
			);
			studyGroupService.sendNewMemberNotification(studyGroup, newGroupMember);

			joinRequestRepository.delete(joinRequest);
		} else if (request.status() == JoinRequestStatus.REJECT) {
			joinRequestRepository.delete(joinRequest);
		}
		log.info("success to approve/reject for join request group = {}", studyGroup.getId());
	}

}
