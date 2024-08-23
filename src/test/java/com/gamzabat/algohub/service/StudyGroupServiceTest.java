package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import com.gamzabat.algohub.feature.studygroup.domain.BookmarkedStudyGroup;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.CreateGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.EditGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupListsResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupResponse;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.BookmarkedStudyGroupRepository;
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
	private BookmarkedStudyGroupRepository bookmarkedStudyGroupRepository;
	@Mock
	private ImageService imageService;
	private User user;
	private User user2;
	private StudyGroup group;
	private final Long groupId = 10L;
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
		List<BookmarkedStudyGroup> bookmarks = new ArrayList<>();
		bookmarks.add(BookmarkedStudyGroup.builder().studyGroup(group).user(user).build());
		bookmarks.add(BookmarkedStudyGroup.builder().studyGroup(group).user(user2).build());

		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(bookmarkedStudyGroupRepository.findAllByStudyGroup(group)).thenReturn(bookmarks);
		// when
		studyGroupService.deleteGroup(user, 10L);
		// then
		verify(studyGroupRepository, times(1)).delete(group);
		verify(bookmarkedStudyGroupRepository, times(1)).deleteAll(bookmarks);
	}

	@Test
	@DisplayName("그룹 삭제 성공 (멤버)")
	void exitGroup() {
		// given
		BookmarkedStudyGroup bookmark = BookmarkedStudyGroup.builder().studyGroup(group).user(user2).build();

		GroupMember groupMember = GroupMember.builder().studyGroup(group).user(user2).joinDate(LocalDate.now()).build();
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(groupMember));
		when(bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user2, group)).thenReturn(
			Optional.ofNullable(bookmark));
		// when
		studyGroupService.deleteGroup(user2, 10L);
		// then
		verify(groupMemberRepository, times(1)).delete(groupMember);
		verify(bookmarkedStudyGroupRepository, times(1)).delete(Objects.requireNonNull(bookmark));
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

	@Test
	@DisplayName("그룹 목록 조회")
	void getGroupList() {
		// given
		List<StudyGroup> groups = new ArrayList<>(30);
		for (int i = 0; i < 10; i++) {
			groups.add(StudyGroup.builder()
				.name("name" + i)
				.owner(user)
				.startDate(LocalDate.now().minusDays(i + 30))
				.endDate(LocalDate.now().minusDays(30))
				.build());
		}
		for (int i = 0; i < 10; i++) {
			groups.add(StudyGroup.builder()
				.name("name" + i)
				.owner(user)
				.startDate(LocalDate.now().minusDays(i + 1))
				.endDate(LocalDate.now().plusDays(i + 1))
				.build());
		}
		for (int i = 0; i < 10; i++) {
			groups.add(StudyGroup.builder()
				.name("name" + i)
				.owner(user)
				.startDate(LocalDate.now().plusDays(30))
				.endDate(LocalDate.now().plusDays(i + 30))
				.build());
		}
		List<BookmarkedStudyGroup> bookmarks = new ArrayList<>(10);
		for (int i = 10; i < 20; i++) {
			bookmarks.add(BookmarkedStudyGroup.builder()
				.studyGroup(groups.get(i))
				.user(user)
				.build());
		}
		when(bookmarkedStudyGroupRepository.findAllByUser(user)).thenReturn(bookmarks);
		when(studyGroupRepository.findByUser(user)).thenReturn(groups);
		// when
		GetStudyGroupListsResponse result = studyGroupService.getStudyGroupList(user);
		// then
		List<GetStudyGroupResponse> bookmarked = result.getBookmarked();
		List<GetStudyGroupResponse> done = result.getDone();
		List<GetStudyGroupResponse> inProgress = result.getInProgress();
		List<GetStudyGroupResponse> queued = result.getQueued();
		assertThat(bookmarked.size()).isEqualTo(10);
		assertThat(done.size()).isEqualTo(10);
		assertThat(inProgress.size()).isEqualTo(10);
		assertThat(queued.size()).isEqualTo(10);
		for (int i = 0; i < 10; i++) {
			assertThat(done.get(i).name()).isEqualTo("name" + i);
			assertThat(done.get(i).ownerNickname()).isEqualTo("nickname");
			assertThat(done.get(i).startDate()).isEqualTo(LocalDate.now().minusDays(i + 30));
			assertThat(done.get(i).endDate()).isEqualTo(LocalDate.now().minusDays(30));
		}
		for (int i = 0; i < 10; i++) {
			assertThat(inProgress.get(i).name()).isEqualTo("name" + i);
			assertThat(inProgress.get(i).ownerNickname()).isEqualTo("nickname");
			assertThat(inProgress.get(i).startDate()).isEqualTo(LocalDate.now().minusDays(i + 1));
			assertThat(inProgress.get(i).endDate()).isEqualTo(LocalDate.now().plusDays(i + 1));
		}
		for (int i = 0; i < 10; i++) {
			assertThat(queued.get(i).name()).isEqualTo("name" + i);
			assertThat(queued.get(i).ownerNickname()).isEqualTo("nickname");
			assertThat(queued.get(i).startDate()).isEqualTo(LocalDate.now().plusDays(30));
			assertThat(queued.get(i).endDate()).isEqualTo(LocalDate.now().plusDays(i + 30));
		}
		for (int i = 0; i < 10; i++) {
			assertThat(bookmarked.get(i).name()).isEqualTo("name" + i);
			assertThat(bookmarked.get(i).ownerNickname()).isEqualTo("nickname");
			assertThat(bookmarked.get(i).startDate()).isEqualTo(LocalDate.now().minusDays(i + 1));
			assertThat(bookmarked.get(i).endDate()).isEqualTo(LocalDate.now().plusDays(i + 1));
		}
	}

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

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 추가 성공 (주인)")
	void updateBookmarkStudyGroup_1() {
		// given
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.ofNullable(group));
		when(bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user, group)).thenReturn(
			Optional.empty());
		// when
		String response = studyGroupService.updateBookmarkGroup(user, groupId);
		// then
		assertThat(response).isEqualTo("스터디 그룹 즐겨찾기 추가 성공");
	}

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 추가 성공 (멤버)")
	void updateBookmarkStudyGroup_2() {
		// given
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		when(bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user2, group)).thenReturn(
			Optional.empty());
		// when
		String response = studyGroupService.updateBookmarkGroup(user2, groupId);
		// then
		assertThat(response).isEqualTo("스터디 그룹 즐겨찾기 추가 성공");
	}

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 삭제 성공 (주인)")
	void updateBookmarkStudyGroup_3() {
		// given
		BookmarkedStudyGroup bookmarkedStudyGroup = BookmarkedStudyGroup.builder().user(user).studyGroup(group).build();
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.ofNullable(group));
		when(bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user, group)).thenReturn(
			Optional.of(bookmarkedStudyGroup));
		// when
		String response = studyGroupService.updateBookmarkGroup(user, groupId);
		// then
		assertThat(response).isEqualTo("스터디 그룹 즐겨찾기 삭제 성공");
	}

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 삭제 성공 (멤버)")
	void updateBookmarkStudyGroup_4() {
		// given
		BookmarkedStudyGroup bookmarkedStudyGroup = BookmarkedStudyGroup.builder()
			.user(user2)
			.studyGroup(group)
			.build();
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		when(bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user2, group)).thenReturn(
			Optional.of(bookmarkedStudyGroup));
		// when
		String response = studyGroupService.updateBookmarkGroup(user2, groupId);
		// then
		assertThat(response).isEqualTo("스터디 그룹 즐겨찾기 삭제 성공");
	}

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 추가/삭제 실패 : 존재하지 않는 그룹")
	void updateBookmarkedStudyGroupFailed_1() {
		// given
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateBookmarkGroup(user, groupId))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 추가/삭제 실패 : 참여하지 않은 그룹")
	void updateBookmarkedStudyGroupFailed_2() {
		// given
		when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.of(group));
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateBookmarkGroup(user2, groupId))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}
}