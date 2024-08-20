package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
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
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.CreateGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.EditGroupRequest;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.studygroup.service.StudyGroupService;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {
	@InjectMocks
	private StudyGroupService studyGroupService;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private ImageService imageService;
	private User user;
	private User user2;
	private StudyGroup group;
	@Captor
	private ArgumentCaptor<StudyGroup> groupCaptor;
	@Captor
	private ArgumentCaptor<GroupMember> memberCaptor;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		group = StudyGroup.builder()
			.name("name")
			.owner(user)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.groupImage("imageUrl")
			.groupCode("code")
			.build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group, 10L);
	}

	@Test
	@DisplayName("그룹 생성 성공")
	void createGroup() {
		// given
		String name = "name";
		String imageUrl = "groupImage";
		MockMultipartFile profileImage = new MockMultipartFile("image", new byte[] {1, 2, 3});
		CreateGroupRequest request = new CreateGroupRequest(name, LocalDate.now(), LocalDate.now().plusDays(5),
			"introduction");
		when(imageService.saveImage(profileImage)).thenReturn(imageUrl);
		// when
		studyGroupService.createGroup(user, request, profileImage);
		// then
		verify(studyGroupRepository, times(1)).save(groupCaptor.capture());
		StudyGroup result = groupCaptor.getValue();
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getOwner()).isEqualTo(user);
		assertThat(result.getStartDate()).isEqualTo(LocalDate.now());
		assertThat(result.getEndDate()).isEqualTo(LocalDate.now().plusDays(5));
		assertThat(result.getIntroduction()).isEqualTo("introduction");
		assertThat(result.getGroupImage()).isEqualTo(imageUrl);
	}

	@Test
	@DisplayName("코드 사용한 그룹 참여 성공")
	void joinGroupWithCode() {
		// given
		when(studyGroupRepository.findByGroupCode("code")).thenReturn(Optional.ofNullable(group));
		// when
		studyGroupService.joinGroupWithCode(user2, "code");
		// then
		verify(groupMemberRepository, times(1)).save(memberCaptor.capture());
		GroupMember result = memberCaptor.getValue();
		assertThat(result.getStudyGroup()).isEqualTo(group);
		assertThat(result.getUser()).isEqualTo(user2);
	}

	@Test
	@DisplayName("코드 사용한 그룹 참여 실패 : 존재하지 않는 그룹")
	void joinGroupWithCodeFailed_1() {
		// given
		when(studyGroupRepository.findByGroupCode("code")).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.joinGroupWithCode(user2, "code"))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("코드 사용한 그룹 참여 실패 : 이미 참여한 그룹 (주인)")
	void joinGroupWithCodeFailed_2() {
		// given
		when(studyGroupRepository.findByGroupCode("code")).thenReturn(Optional.ofNullable(group));
		// when, then
		assertThatThrownBy(() -> studyGroupService.joinGroupWithCode(user, "code"))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("error", "이미 참여한 그룹 입니다.");
	}

	@Test
	@DisplayName("코드 사용한 그룹 참여 실패 : 이미 참여한 그룹 (멤버)")
	void joinGroupWithCodeFailed_3() {
		// given
		when(studyGroupRepository.findByGroupCode("code")).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		// when, then
		assertThatThrownBy(() -> studyGroupService.joinGroupWithCode(user2, "code"))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("error", "이미 참여한 그룹 입니다.");
	}

	@Test
	@DisplayName("그룹 삭제 성공 (주인)")
	void deleteGroup() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		// when
		studyGroupService.deleteGroup(user, 10L);
		// then
		verify(studyGroupRepository, times(1)).delete(group);
	}

	@Test
	@DisplayName("그룹 삭제 성공 (멤버)")
	void exitGroup() {
		// given
		GroupMember groupMember = GroupMember.builder().studyGroup(group).user(user2).joinDate(LocalDate.now()).build();
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(groupMember));
		// when
		studyGroupService.deleteGroup(user2, 10L);
		// then
		verify(groupMemberRepository, times(1)).delete(groupMember);
	}

	@Test
	@DisplayName("그룹 삭제 실패 : 존재하지 않는 그룹")
	void deleteGroupFailed_1() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.deleteGroup(user, 10L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("그룹 삭제 실패 : 이미 참여하지 않은 그룹")
	void deleteGroupFailed_2() {
		// given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.deleteGroup(user2, 10L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "이미 참여하지 않은 그룹 입니다.");
	}

	//	@Test
	//	@DisplayName("그룹 목록 조회")
	//	void getGroupList(){
	//		// given
	//		List<StudyGroup> groups = new ArrayList<>(30);
	//		for(int i=0; i<30; i++){
	//			groups.add(StudyGroup.builder()
	//				.name("name"+i)
	//				.owner(user)
	//				.groupImage("imageUrl"+i)
	//					.startDate(LocalDate.now())
	//					.endDate(LocalDate.now().plusDays(i))
	//					.introduction("introduction"+i)
	//				.groupCode("code"+i)
	//				.build());
	//		}
	//		when(studyGroupRepository.findByUser(user)).thenReturn(groups);
	//		// when
	//		List<GetStudyGroupResponse> result = studyGroupService.getStudyGroupList(user);
	//		// then
	//		assertThat(result.size()).isEqualTo(30);
	//		for(int i=0; i<30; i++){
	//			assertThat(result.get(i).name()).isEqualTo("name"+i);
	//			assertThat(result.get(i).ownerNickname()).isEqualTo("nickname");
	//			assertThat(result.get(i).groupImage()).isEqualTo("imageUrl"+i);
	//			assertThat(result.get(i).startDate()).isEqualTo(LocalDate.now());
	//			assertThat(result.get(i).endDate()).isEqualTo(LocalDate.now().plusDays(i));
	//			assertThat(result.get(i).introduction()).isEqualTo("introduction"+i);
	//			assertThat(result.get(i).isOwner()).isTrue();
	//		}
	//	}

	@Test
	@DisplayName("그룹 정보 수정 성공")
	void editGroup() {
		// given
		EditGroupRequest request = new EditGroupRequest(10L, "editName", LocalDate.now().plusDays(10),
			LocalDate.now().plusDays(10), "editIntroduction");
		MockMultipartFile editImage = new MockMultipartFile("editImage", new byte[] {1, 2, 3});
		when(imageService.saveImage(editImage)).thenReturn("editImage");
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.ofNullable(group));
		// when
		studyGroupService.editGroup(user, request, editImage);
		// then
		assertThat(group.getName()).isEqualTo("editName");
		assertThat(group.getGroupImage()).isEqualTo("editImage");
		assertThat(group.getStartDate()).isEqualTo(LocalDate.now().plusDays(10));
		assertThat(group.getEndDate()).isEqualTo(LocalDate.now().plusDays(10));
		assertThat(group.getIntroduction()).isEqualTo("editIntroduction");
	}

	@Test
	@DisplayName("그룹 정보 수정 실패 : 존재하지 않는 그룹")
	void editGroupFailed_1() {
		// given
		EditGroupRequest request = new EditGroupRequest(10L, "editName", LocalDate.now().plusDays(10),
			LocalDate.now().plusDays(10), "editIntroduction");
		MockMultipartFile editImage = new MockMultipartFile("editImage", new byte[] {1, 2, 3});
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.editGroup(user, request, editImage))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("그룹 정보 수정 실패 : 권한 없음")
	void editGroupFailed_2() {
		// given
		EditGroupRequest request = new EditGroupRequest(10L, "editName", LocalDate.now().plusDays(10),
			LocalDate.now().plusDays(10), "editIntroduction");
		MockMultipartFile editImage = new MockMultipartFile("editImage", new byte[] {1, 2, 3});
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when, then
		assertThatThrownBy(() -> studyGroupService.editGroup(user2, request, editImage))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "그룹 정보 수정에 대한 권한이 없습니다.");
	}

}