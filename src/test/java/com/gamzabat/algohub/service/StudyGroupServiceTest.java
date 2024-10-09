package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.BookmarkedStudyGroup;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.Rank;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.CreateGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.EditGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.GetGroupMemberResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupListsResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupResponse;
import com.gamzabat.algohub.feature.studygroup.dto.UpdateGroupMemberRoleRequest;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundRankException;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.BookmarkedStudyGroupRepository;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.RankRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.studygroup.service.StudyGroupService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

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
	private SolutionRepository solutionRepository;
	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private RankRepository rankRepository;
	@Mock
	private ImageService imageService;
	private User user, owner, user2, user3, user4;
	private StudyGroup group;
	private Problem problem1, problem2;
	private Solution solution1, solution2, solution3;
	private final Long groupId = 10L;
	private GroupMember ownerGroupmember;
	private GroupMember groupMember1, groupMember2, groupMember3, groupMember4;
	private Rank rank1, rank2, rank3, rank4;
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
		user4 = User.builder().email("email4").password("password").nickname("nickname4")
			.role(Role.USER).profileImage("image4").build();
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
			.build();
		groupMember1 = GroupMember.builder()
			.studyGroup(group)
			.user(user)
			.role(RoleOfGroupMember.OWNER)
			.build();
		groupMember2 = GroupMember.builder()
			.studyGroup(group)
			.user(user2)
			.role(RoleOfGroupMember.PARTICIPANT)
			.build();
		groupMember3 = GroupMember.builder()
			.studyGroup(group)
			.user(user3)
			.role(RoleOfGroupMember.ADMIN)
			.build();
		groupMember4 = GroupMember.builder()
			.studyGroup(group)
			.user(user4)
			.role(RoleOfGroupMember.PARTICIPANT)
			.build();

		problem1 = Problem.builder()
			.studyGroup(group)
			.build();
		problem2 = Problem.builder()
			.studyGroup(group)
			.build();
		solution1 = Solution.builder()
			.result("맞았습니다!!")
			.solvedDateTime(LocalDateTime.now().minusDays(1))
			.problem(problem1)
			.user(user)
			.build();
		solution2 = Solution.builder()
			.result("맞았습니다!!")
			.solvedDateTime(LocalDateTime.now().minusDays(2))
			.problem(problem2)
			.user(user)
			.build();
		solution3 = Solution.builder()
			.result("맞았습니다!!")
			.solvedDateTime(LocalDateTime.now().minusDays(1))
			.problem(problem1)
			.user(user2)
			.build();

		rank1 = Rank.builder()
			.member(groupMember1)
			.solvedCount(3)
			.currentRank(1)
			.rankDiff("-")
			.build();
		rank2 = Rank.builder()
			.member(groupMember2)
			.solvedCount(2)
			.currentRank(2)
			.rankDiff("+1")
			.build();
		rank3 = Rank.builder()
			.member(groupMember3)
			.solvedCount(1)
			.currentRank(3)
			.rankDiff("-1")
			.build();
		rank4 = Rank.builder()
			.member(groupMember4)
			.solvedCount(0)
			.currentRank(4)
			.rankDiff("-")
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
	@DisplayName("코드 사용한 그룹 참여 실패 : 이미 참여한 그룹")
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
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when
		studyGroupService.deleteGroup(user, 10L);
		// then
		verify(studyGroupRepository, times(1)).delete(group);
		verify(groupMemberRepository, times(1)).delete(groupMember1);
		verify(bookmarkedStudyGroupRepository, times(1)).deleteAll(bookmarks);
	}

	@Test
	@DisplayName("그룹 삭제 성공 (멤버)")
	void exitGroup() {
		// given
		BookmarkedStudyGroup bookmark = BookmarkedStudyGroup.builder().studyGroup(group).user(user2).build();

		GroupMember groupMember = GroupMember.builder()
			.studyGroup(group)
			.user(user2)
			.role(RoleOfGroupMember.ADMIN)
			.joinDate(LocalDate.now())
			.build();
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
			StudyGroup group = StudyGroup.builder()
				.name("name" + i)
				.startDate(LocalDate.now().minusDays(i + 30))
				.endDate(LocalDate.now().minusDays(30))
				.build();
			groups.add(group);
			when(groupMemberRepository.findByStudyGroupAndRole(group, RoleOfGroupMember.OWNER)).thenReturn(
				ownerGroupmember);
		}
		for (int i = 0; i < 10; i++) {
			StudyGroup group = StudyGroup.builder()
				.name("name" + i)
				.startDate(LocalDate.now().minusDays(i))
				.endDate(LocalDate.now().plusDays(i))
				.build();
			groups.add(group);
			when(groupMemberRepository.findByStudyGroupAndRole(group, RoleOfGroupMember.OWNER)).thenReturn(
				ownerGroupmember);
		}
		for (int i = 0; i < 10; i++) {
			StudyGroup group = StudyGroup.builder()
				.name("name" + i)
				.startDate(LocalDate.now().plusDays(30))
				.endDate(LocalDate.now().plusDays(i + 30))
				.build();
			groups.add(group);
			when(groupMemberRepository.findByStudyGroupAndRole(group, RoleOfGroupMember.OWNER)).thenReturn(
				groupMember2);
		}
		List<BookmarkedStudyGroup> bookmarks = new ArrayList<>(10);
		for (int i = 0; i < 10; i++) {
			bookmarks.add(BookmarkedStudyGroup.builder()
				.studyGroup(groups.get(i))
				.user(user)
				.build());
			when(bookmarkedStudyGroupRepository.existsByUserAndStudyGroup(user, groups.get(i))).thenReturn(true);
		}
		when(bookmarkedStudyGroupRepository.findAllByUser(user)).thenReturn(bookmarks);
		when(studyGroupRepository.findAllByUser(user)).thenReturn(groups);
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
			assertThat(done.get(i).ownerNickname()).isEqualTo("nickname1");
			assertThat(done.get(i).startDate()).isEqualTo(LocalDate.now().minusDays(i + 30));
			assertThat(done.get(i).endDate()).isEqualTo(LocalDate.now().minusDays(30));
			assertThat(done.get(i).isBookmarked()).isTrue();
			assertThat(done.get(i).isOwner()).isTrue();
		}
		for (int i = 0; i < 10; i++) {
			assertThat(inProgress.get(i).name()).isEqualTo("name" + i);
			assertThat(inProgress.get(i).ownerNickname()).isEqualTo("nickname1");
			assertThat(inProgress.get(i).startDate()).isEqualTo(LocalDate.now().minusDays(i));
			assertThat(inProgress.get(i).endDate()).isEqualTo(LocalDate.now().plusDays(i));
			assertThat(inProgress.get(i).isBookmarked()).isFalse();
			assertThat(inProgress.get(i).isOwner()).isTrue();
		}
		for (int i = 0; i < 10; i++) {
			assertThat(queued.get(i).name()).isEqualTo("name" + i);
			assertThat(queued.get(i).ownerNickname()).isEqualTo("nickname2");
			assertThat(queued.get(i).startDate()).isEqualTo(LocalDate.now().plusDays(30));
			assertThat(queued.get(i).endDate()).isEqualTo(LocalDate.now().plusDays(i + 30));
			assertThat(queued.get(i).isBookmarked()).isFalse();
			assertThat(queued.get(i).isOwner()).isFalse();
		}
		for (int i = 0; i < 10; i++) {
			assertThat(bookmarked.get(i).name()).isEqualTo("name" + i);
			assertThat(bookmarked.get(i).ownerNickname()).isEqualTo("nickname1");
			assertThat(bookmarked.get(i).startDate()).isEqualTo(LocalDate.now().minusDays(i + 30));
			assertThat(bookmarked.get(i).endDate()).isEqualTo(LocalDate.now().minusDays(30));
			assertThat(bookmarked.get(i).isBookmarked()).isTrue();
			assertThat(bookmarked.get(i).isOwner()).isTrue();
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
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
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
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.ofNullable(groupMember2));
		// when, then
		assertThatThrownBy(() -> studyGroupService.editGroup(user2, request, editImage))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "그룹 정보 수정에 대한 권한이 없습니다.");
	}

	@Test
	@DisplayName("스터디 그룹 회원 목록 조회")
	void getGroupMemberList() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		List<GroupMember> groupMemberList = new ArrayList<>();
		groupMemberList.add(groupMember1);
		groupMemberList.add(groupMember2);
		groupMemberList.add(groupMember3);
		when(groupMemberRepository.findAllByStudyGroup(group)).thenReturn(groupMemberList);
		// when
		List<GetGroupMemberResponse> response = studyGroupService.getGroupMemberList(user, groupId);
		// then
		assertThat(response.get(0).getMemberId()).isEqualTo(1);
		assertThat(response.get(0).getNickname()).isEqualTo("nickname1");
		assertThat(response.get(0).getRole()).isEqualTo(RoleOfGroupMember.OWNER);
		assertThat(response.get(1).getMemberId()).isEqualTo(3);
		assertThat(response.get(1).getNickname()).isEqualTo("nickname3");
		assertThat(response.get(1).getRole()).isEqualTo(RoleOfGroupMember.ADMIN);
		assertThat(response.get(2).getMemberId()).isEqualTo(2);
		assertThat(response.get(2).getRole()).isEqualTo(RoleOfGroupMember.PARTICIPANT);
		assertThat(response.get(2).getNickname()).isEqualTo("nickname2");
	}

	@Test
	@DisplayName("스터디 그룹 회원 목록 조회 실패 : 존재하지 않는 그룹")
	void getGroupMemberListFailed_1() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.getGroupMemberList(user, groupId))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "그룹을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("스터디 그룹 회원 목록 조회 실패 : 참여하지 않은 그룹")
	void getGroupMemberListFailed_2() {
		// given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> studyGroupService.getGroupMemberList(user, groupId))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "그룹 정보를 확인할 권한이 없습니다");
	}

	@Test
	@DisplayName("스터디 그룹 즐겨찾기 추가 성공")
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
	@DisplayName("스터디 그룹 즐겨찾기 삭제 성공")
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

	@Test
	@DisplayName("전체랭킹 조회 성공")
	void getAllRank_SuccessByOwner() {
		//given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		List<Rank> ranking = new ArrayList<>();
		ranking.add(rank2);
		ranking.add(rank3);
		ranking.add(rank1);
		ranking.add(rank4);
		when(rankRepository.findAllByStudyGroup(group)).thenReturn(ranking);

		//when
		List<GetRankingResponse> result = studyGroupService.getAllRank(user2, 10L);

		//then
		assertThat(result.size()).isEqualTo(4);

		for (int i = 0; i < 4; i++) {
			assertThat(result.get(i).getProfileImage()).isEqualTo("image" + (i + 1));
			assertThat(result.get(i).getSolvedCount()).isEqualTo(3 - i);
			assertThat(result.get(i).getUserNickname()).isEqualTo("nickname" + (i + 1));
			assertThat(result.get(i).getRank()).isEqualTo(i + 1);
		}
	}

	@Test
	@DisplayName("전체랭킹 조회 실패 : 그룹을 못 찾은 경우")
	void getAllRank_FailedByCannotFoundGroup() {
		//given
		when(studyGroupRepository.findById(9L)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> studyGroupService.getAllRank(user, 9L))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "그룹을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("전체랭킹 조회 실패 : 랭킹을 확인할 권한이 없는 경우")
	void getAllRank_FailedByAccess() {
		//given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);

		//then
		assertThatThrownBy(() -> studyGroupService.getAllRank(user2, groupId))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("errors", "랭킹을 확인할 권한이 없습니다.");
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 성공")
	void getTopRanking() {
		//given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		List<Rank> ranking = new ArrayList<>();
		ranking.add(rank2);
		ranking.add(rank3);
		ranking.add(rank4);
		ranking.add(rank1);
		when(rankRepository.findAllByStudyGroup(group)).thenReturn(ranking);

		//when
		List<GetRankingResponse> result = studyGroupService.getTopRank(user2, 10L);

		//then
		assertThat(result.size()).isEqualTo(3);

		for (int i = 0; i < 3; i++) {
			assertThat(result.get(i).getProfileImage()).isEqualTo("image" + (i + 1));
			assertThat(result.get(i).getSolvedCount()).isEqualTo(3 - i);
			assertThat(result.get(i).getUserNickname()).isEqualTo("nickname" + (i + 1));
			assertThat(result.get(i).getRank()).isEqualTo(i + 1);
		}
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 성공 : 모두 0 solved인 경우")
	void getTopRanking_EmptyRank() {
		//given
		when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		List<Rank> ranking = new ArrayList<>();
		ranking.add(Rank.builder()
			.member(groupMember1)
			.solvedCount(0)
			.build());
		ranking.add(Rank.builder()
			.member(groupMember2)
			.solvedCount(0)
			.build());
		ranking.add(Rank.builder()
			.member(groupMember3)
			.solvedCount(0)
			.build());
		ranking.add(Rank.builder()
			.member(groupMember4)
			.solvedCount(0)
			.build());
		when(rankRepository.findAllByStudyGroup(group)).thenReturn(ranking);

		//when
		List<GetRankingResponse> result = studyGroupService.getTopRank(user2, 10L);

		//then
		assertThat(result.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 실패 : 그룹을 못 찾은 경우")
	void getTopRanking_FailedByCannotFoundGroup() {
		//given
		when(studyGroupRepository.findById(9L)).thenReturn(Optional.empty());

		//then
		assertThatThrownBy(() -> studyGroupService.getTopRank(user, 9L))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "그룹을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 실패 : 랭킹을 확인할 권한이 없는 경우")
	void getTopRanking_FailedByAccess() {
		//given
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);

		//then
		assertThatThrownBy(() -> studyGroupService.getTopRank(user2, groupId))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("errors", "랭킹을 확인할 권한이 없습니다.");
	}

	@Test
	@DisplayName("스터디 그룹 멤버 역할 수정 성공")
	void updateGroupMemberRole() {
		// given
		UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 2L, "ADMIN");
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.ofNullable(groupMember2));
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user2));
		// when
		studyGroupService.updateGroupMemberRole(user, request);
		// then
		assertThat(groupMember2.getUser()).isEqualTo(user2);
		assertThat(groupMember2.getStudyGroup()).isEqualTo(group);
		assertThat(groupMember2.getRole()).isEqualTo(RoleOfGroupMember.ADMIN);
	}

	@Test
	@DisplayName("스터디 그룹 멤버 역할 수정 실패 : 존재하지 않는 그룹")
	void updateGroupMemberRoleFailed_1() {
		// given
		UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 2L, "ADMIN");
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateGroupMemberRole(user, request))
			.isInstanceOf(CannotFoundGroupException.class)
			.hasFieldOrPropertyWithValue("errors", "존재하지 않는 그룹입니다.");
	}

	@Test
	@DisplayName("스터디 그룹 멤버 역할 수정 실패 : 역할 수정 권한 없음")
	void updateGroupMemberRoleFailed_2() {
		// given
		UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 2L, "ADMIN");
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.ofNullable(groupMember2));
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateGroupMemberRole(user2, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹의 멤버 역할을 수정할 권한이 없습니다.");
	}

	@Test
	@DisplayName("스터디 그룹 멤버 역할 수정 실패 : 존재하지 않는 회원")
	void updateGroupMemberRoleFailed_3() {
		// given
		UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 2L, "ADMIN");
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateGroupMemberRole(user, request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "존재하지 않는 회원입니다.");
	}

	@Test
	@DisplayName("스터디 그룹 멤버 역할 수정 실패 : 참여하지 않은 회원")
	void updateGroupMemberRoleFailed_4() {
		// given
		UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 2L, "ADMIN");
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateGroupMemberRole(user, request))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "해당 스터디 그룹에 참여하지 않은 회원입니다.");
	}

	@Test
	@DisplayName("랭킹 업데이트 성공")
	void updateRanking() {
		// given
		List<Rank> ranks = new ArrayList<>();
		ranks.add(rank1);
		ranks.add(rank2);
		ranks.add(rank3);
		when(rankRepository.findAllByStudyGroup(group)).thenReturn(ranks);
		when(rankRepository.findByMember(groupMember3)).thenReturn(Optional.of(rank3));

		// when - user3의 랭킹이 +2
		studyGroupService.updateRanking(groupMember3);
		studyGroupService.updateRanking(groupMember3);
		studyGroupService.updateRanking(groupMember3);

		// then
		assertThat(rank3.getMember()).isEqualTo(groupMember3);
		assertThat(rank3.getCurrentRank()).isEqualTo(1);
		assertThat(rank3.getSolvedCount()).isEqualTo(4);
		assertThat(rank3.getRankDiff()).isEqualTo("+1");

		assertThat(rank1.getMember()).isEqualTo(groupMember1);
		assertThat(rank1.getCurrentRank()).isEqualTo(2);
		assertThat(rank1.getSolvedCount()).isEqualTo(3);
		assertThat(rank1.getRankDiff()).isEqualTo("-1");

		assertThat(rank2.getMember()).isEqualTo(groupMember2);
		assertThat(rank2.getCurrentRank()).isEqualTo(3);
		assertThat(rank2.getSolvedCount()).isEqualTo(2);
		assertThat(rank2.getRankDiff()).isEqualTo("-");
	}

	@Test
	@DisplayName("랭킹 업데이트 실패 : 유저 랭킹 정보 조회 실패")
	void updateRankingFailed_1() {
		// given
		when(rankRepository.findByMember(groupMember3)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> studyGroupService.updateRanking(groupMember3))
			.isInstanceOf(CannotFoundRankException.class)
			.hasFieldOrPropertyWithValue("error", "유저의 랭킹 정보를 조회할 수 없습니다.");
	}
}