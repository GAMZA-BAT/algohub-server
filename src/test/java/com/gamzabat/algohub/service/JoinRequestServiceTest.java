package com.gamzabat.algohub.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gamzabat.algohub.enums.JoinRequestStatus;
import com.gamzabat.algohub.enums.Role;
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
import com.gamzabat.algohub.feature.group.studygroup.service.JoinRequestService;
import com.gamzabat.algohub.feature.group.studygroup.service.StudyGroupService;
import com.gamzabat.algohub.feature.notification.repository.NotificationSettingRepository;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class JoinRequestServiceTest {
	@InjectMocks
	private JoinRequestService joinRequestService;
	@Mock
	private StudyGroupService studyGroupService;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private JoinRequestRepository joinRequestRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private NotificationSettingRepository notificationSettingRepository;
	@Mock
	private RankingRepository rankingRepository;
	
	private User user, owner, user2, user3, requester;
	private StudyGroup group;
	private Problem problem1, problem2;
	private GroupMember groupMember1, groupMember2, groupMember3;
	private GroupMember ownerGroupmember;
	private JoinRequest joinRequest;
	@Captor
	private ArgumentCaptor<StudyGroup> groupCaptor;
	@Captor
	private ArgumentCaptor<GroupMember> memberCaptor;

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
		requester = User.builder().email("eamilRequester").password("password").nickname("requester")
			.role(Role.USER).profileImage("imageForRequester").build();

		group = StudyGroup.builder()
			.name("name")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.groupImage("imageUrl")
			.groupCode("code")
			.build();
		ownerGroupmember = GroupMember.builder()
			.studyGroup(group)
			.user(owner)
			.role(RoleOfGroupMember.OWNER)
			.joinDate(LocalDate.now())
			.build();
		groupMember1 = GroupMember.builder()
			.studyGroup(group)
			.user(user)
			.role(RoleOfGroupMember.OWNER)
			.joinDate(LocalDate.now())
			.build();
		groupMember2 = GroupMember.builder()
			.studyGroup(group)
			.user(user2)
			.role(RoleOfGroupMember.PARTICIPANT)
			.joinDate(LocalDate.now())
			.build();
		groupMember3 = GroupMember.builder()
			.studyGroup(group)
			.user(user3)
			.role(RoleOfGroupMember.ADMIN)
			.joinDate(LocalDate.now())
			.build();

		problem1 = Problem.builder()
			.studyGroup(group)
			.build();
		problem2 = Problem.builder()
			.studyGroup(group)
			.build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(owner, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);
		userField.set(requester, 4L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group, 10L);

		Field memberId = GroupMember.class.getDeclaredField("id");
		memberId.setAccessible(true);
		memberId.set(groupMember1, 100L);
		memberId.set(groupMember2, 200L);
		memberId.set(groupMember3, 300L);
		//For Join Request Service Test
		joinRequest = new JoinRequest(group, requester);
		Field requestId = JoinRequest.class.getDeclaredField("id");
		Field requestGroup = JoinRequest.class.getDeclaredField("group");
		requestGroup.setAccessible(true);
		requestGroup.set(joinRequest, group);
		requestId.setAccessible(true);
		requestId.set(joinRequest, 1000L);

	}

	@Test
	@DisplayName("그룹 가입 요청 성공")
	void joinRequest_Success() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(requester, group)).thenReturn(false);
		when(joinRequestRepository.existsByGroup_IdAndRequester_Id(group.getId(), requester.getId()))
			.thenReturn(false);

		// when
		joinRequestService.joinRequest(requester, 10L);

		// then
		ArgumentCaptor<JoinRequest> captor = ArgumentCaptor.forClass(JoinRequest.class);
		verify(joinRequestRepository, times(1)).save(captor.capture());
		JoinRequest savedRequest = captor.getValue();

		assertThat(savedRequest.getRequester()).isEqualTo(requester);
		assertThat(savedRequest.getGroup()).isEqualTo(group);
	}

	@Test
	@DisplayName("그룹 가입 요청 실패 : 이미 가입한 그룹")
	void joinRequest_Fail_AlreadyMember() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(requester, group)).thenReturn(true);

		// when, then
		assertThatThrownBy(() -> joinRequestService.joinRequest(requester, 10L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("error", "이미 가입한 그룹입니다");
	}

	@Test
	@DisplayName("그룹 가입 요청 실패 : 이미 요청한 그룹")
	void joinRequest_Fail_AlreadyRequested() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(requester, group)).thenReturn(false);
		when(joinRequestRepository.existsByGroup_IdAndRequester_Id(group.getId(), requester.getId())).thenReturn(
			true);

		// when, then
		assertThatThrownBy(() -> joinRequestService.joinRequest(requester, 10L))
			.isInstanceOf(JoinRequestException.class)
			.hasMessage("이미 요청한 그룹입니다.");
	}

	@Test
	@DisplayName("가입 요청 목록 조회 성공")
	void getAllJoinRequests_Success() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(owner, group)).thenReturn(Optional.of(ownerGroupmember));
		when(joinRequestRepository.findAllByGroup_Id(10L)).thenReturn(List.of(joinRequest));

		// when
		List<JoinRequest> requests = joinRequestService.getAllJoinRequests(owner, 10L);

		// then
		assertThat(requests).hasSize(1);
		assertThat(requests.get(0).getRequester().getNickname()).isEqualTo("requester");
	}

	@Test
	@DisplayName("가입 요청 목록 조회 실패 : 권한 없음")
	void getAllJoinRequests_Fail_NoPermission() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(groupMember2.getUser(), group)).thenReturn(
			Optional.of(groupMember2));

		// when, then
		assertThatThrownBy(() -> joinRequestService.getAllJoinRequests(groupMember2.getUser(), 10L))
			.isInstanceOf(JoinRequestException.class)
			.hasMessage("요청 목록을 조회할 권한이 없습니다.");
	}

	@Test
	@DisplayName("가입 요청 승인 성공")
	void approveJoinRequest_Success() {
		// given
		when(joinRequestRepository.findById(1000L)).thenReturn(Optional.of(joinRequest));
		when(studyGroupRepository.findById(joinRequest.getGroup().getId())).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(owner, group)).thenReturn(Optional.of(ownerGroupmember));
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.APPROVE);
		// when
		joinRequestService.updateJoinRequest(owner, 1000L, request);

		// then
		ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
		verify(groupMemberRepository, times(1)).save(captor.capture());
		GroupMember newMember = captor.getValue();

		assertThat(newMember.getUser()).isEqualTo(requester);
		assertThat(newMember.getRole()).isEqualTo(RoleOfGroupMember.PARTICIPANT);
		verify(joinRequestRepository, times(1)).delete(joinRequest);

	}

	@Test
	@DisplayName("가입 요청 승인 실패 : 요청 없음")
	void approveJoinRequest_Fail_RequestNotFound() {
		// given
		when(joinRequestRepository.findById(100L)).thenReturn(Optional.empty());
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.APPROVE);

		// when, then
		assertThatThrownBy(() -> joinRequestService.updateJoinRequest(owner, 100L, request))
			.isInstanceOf(JoinRequestException.class)
			.hasMessage("해당 요청이 존재하지 않습니다");
	}

	@Test
	@DisplayName("가입 요청 거절 성공 STATUS : PENDING -> REJECT 로 변경")
	void rejectJoinRequest_Success() {
		// given
		when(joinRequestRepository.findById(1000L)).thenReturn(Optional.of(joinRequest));
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(owner, group)).thenReturn(Optional.of(ownerGroupmember));
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.REJECT);

		// when
		joinRequestService.updateJoinRequest(owner, 1000L, request);

		// then
		verify(joinRequestRepository, times(1)).delete(joinRequest);
	}

}
