package com.gamzabat.algohub.feature.group.ranking.service;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.group.studygroup.service.StudyGroupService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class RankingUpdateServiceAOPTest {
	@MockBean
	private RankingRepository rankingRepository;
	@MockBean
	private StudyGroupRepository studyGroupRepository;
	@MockBean
	private GroupMemberRepository groupMemberRepository;
	@Autowired
	private StudyGroupService studyGroupService;
	@MockBean
	private RankingUpdateService rankingUpdateService;

	private User user, owner, user2, user3, user4;
	private StudyGroup group;
	private GroupMember groupMember1, groupMember2;
	private final Long groupId = 10L;
	@MockBean
	private UserRepository userRepository;

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
		user4 = User.builder().email("email4").password("password").nickname("nickname4")
			.role(Role.USER).profileImage("image4").build();
		group = StudyGroup.builder()
			.name("name")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.groupImage("imageUrl")
			.groupCode("code")
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

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(owner, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group, 10L);
	}

	@Test
	@DisplayName("deleteMember 후 AOP 랭킹 업데이트 호출 성공")
	void testAopTriggerAfterDeleteMemberFromStudyGroup() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.of(groupMember1));
		when(userRepository.findById(user2.getId())).thenReturn(Optional.ofNullable(user2));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(groupMember2));
		doNothing().when(rankingUpdateService).updateRanking(group);
		// when
		studyGroupService.deleteMember(user, user2.getId(), groupId);
		// then
		verify(rankingUpdateService, times(1)).updateRanking(group);
	}

	@Test
	@DisplayName("deleteGroup 후 AOP 랭킹 업데이트 호출 성공")
	void testAOPTriggerAfterDeleteGroup() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(groupMember2));
		doNothing().when(rankingUpdateService).updateRanking(group);
		// when
		studyGroupService.deleteGroup(user2, groupId);
		// then
		verify(rankingUpdateService, times(1)).updateRanking(group);
	}
}
