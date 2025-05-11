package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.domain.SolutionComment;
import com.gamzabat.algohub.feature.solution.dto.CreateSolutionRequest;
import com.gamzabat.algohub.feature.solution.dto.GetCurrentSolvingStatusResponse;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionWithGroupIdResponse;
import com.gamzabat.algohub.feature.solution.enums.ProgressCategory;
import com.gamzabat.algohub.feature.solution.exception.CannotFoundSolutionException;
import com.gamzabat.algohub.feature.solution.repository.SolutionCommentRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.solution.service.SolutionService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SolutionServiceTest {
	@InjectMocks
	private SolutionService solutionService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private SolutionRepository solutionRepository;
	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private SolutionCommentRepository solutionCommentRepository;
	@Mock
	private UserRepository userRepository;
	private User user, user2, user3;
	private Problem problem, problem1, problem2;
	private StudyGroup group, group1;
	private Long groupId = 30L;
	private Integer problemNumber = 1010;
	private SolutionComment solutionComment;
	DateTimeFormatter formatter;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		user = User.builder().email("email1").password("password").nickname("nickname1").bjNickname("bjNickname1")
			.role(Role.USER).profileImage("profileImage").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2").bjNickname("bjNickname2")
			.role(Role.USER).profileImage("profileImage").build();
		user3 = User.builder().email("email3").password("password").nickname("nickname3").bjNickname("bjNickname2")
			.role(Role.USER).profileImage("profileImage").build();
		group = StudyGroup.builder().name("name").groupImage("imageUrl").groupCode("code").build();
		group1 = StudyGroup.builder().name("name1").groupImage("imageUrl1").groupCode("code1").build();
		problem = Problem.builder()
			.studyGroup(group)
			.link("link")
			.number(1010)
			.level(100)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now())
			.build();
		problem1 = Problem.builder()
			.studyGroup(group)
			.link("link1")
			.number(1020)
			.level(200)
			.startDate(LocalDate.now().minusDays(30))
			.endDate(LocalDate.now().minusDays(10))
			.build();
		problem2 = Problem.builder()
			.studyGroup(group1)
			.link("link2")
			.number(1030)
			.level(300)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now())
			.build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);

		Field problemField = Problem.class.getDeclaredField("id");
		problemField.setAccessible(true);
		problemField.set(problem, 10L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(group, 30L);
	}

	@Test
	@DisplayName("풀이 목록 조회 성공 : 풀이 결과 필터링")
	void getSolutionList() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> list = new ArrayList<>();
		List<SolutionComment> commentList = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();

		setTestSolutionAndCommentList(list, commentList, fixedDateTime);

		Page<Solution> compileErrorPage = new PageImpl<>(list.subList(0, 10), pageable, 10);
		Page<Solution> correctPage = new PageImpl<>(list.subList(10, 20), pageable, 10);
		List<SolutionComment> readComments = new ArrayList<>(commentList.subList(0, 25));
		List<SolutionComment> unReadComments = new ArrayList<>(commentList.subList(25, 50));

		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(group));
		when(problemRepository.findById(10L)).thenReturn(Optional.ofNullable(problem));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(solutionRepository.findAllFilteredSolutions(problem, null, null, "컴파일 에러", pageable)).thenReturn(
			compileErrorPage);
		for (int i = 0; i < 5; i++) {
			when(solutionCommentRepository.findAllBySolution(compileErrorPage.getContent().get(i))).thenReturn(
				readComments.subList(i * 5, i * 5 + 5));
		}
		when(solutionRepository.findAllFilteredSolutions(problem, null, null, "맞았습니다!!", pageable)).thenReturn(
			correctPage);
		for (int i = 0; i < 5; i++) {
			when(solutionCommentRepository.findAllBySolution(correctPage.getContent().get(i))).thenReturn(
				unReadComments.subList(i * 5, i * 5 + 5));
		}
		// when
		Page<GetSolutionResponse> compileErrorResult = solutionService.getSolutionList(user, 10L, null, null, "컴파일 에러",
			pageable);
		Page<GetSolutionResponse> correctResult = solutionService.getSolutionList(user, 10L, null, null, "맞았습니다!!",
			pageable);
		// then
		// 1) 컴파일 에러 풀이 목록 조회
		assertThat(compileErrorResult.getContent().size()).isEqualTo(10);
		assertThat(compileErrorResult.getTotalElements()).isEqualTo(10);
		for (int i = 0; i < 5; i++) {
			assertThat(compileErrorResult.getContent().get(i).getContent()).isEqualTo("content" + i);
			assertThat(compileErrorResult.getContent().get(i).getResult()).isEqualTo("컴파일 에러");
			assertThat(compileErrorResult.getContent().get(i).getMemoryUsage()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).getExecutionTime()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(compileErrorResult.getContent().get(i).getLanguage()).isEqualTo("Java 11");
			assertThat(compileErrorResult.getContent().get(i).getIsRead()).isEqualTo(true);
			assertThat(compileErrorResult.getContent().get(i).getSolvedDateTime()).isEqualTo(
				DateFormatUtil.formatDateTime(fixedDateTime));
		}
		for (int i = 5; i < 10; i++) {
			assertThat(compileErrorResult.getContent().get(i).getContent()).isEqualTo("content" + i);
			assertThat(compileErrorResult.getContent().get(i).getResult()).isEqualTo("컴파일 에러");
			assertThat(compileErrorResult.getContent().get(i).getMemoryUsage()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).getExecutionTime()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).getNickname()).isEqualTo("nickname2");
			assertThat(compileErrorResult.getContent().get(i).getLanguage()).isEqualTo("C++17");
			assertThat(compileErrorResult.getContent().get(i).getIsRead()).isEqualTo(true);
			assertThat(compileErrorResult.getContent().get(i).getSolvedDateTime()).isEqualTo(
				DateFormatUtil.formatDateTime(fixedDateTime));
		}
		// 2) 맞았습니다!! 풀이 목록 조회
		assertThat(correctResult.getContent().size()).isEqualTo(10);
		assertThat(correctResult.getTotalElements()).isEqualTo(10);
		for (int i = 0; i < 5; i++) {
			assertThat(correctResult.getContent().get(i).getContent()).isEqualTo("content" + (i + 10));
			assertThat(correctResult.getContent().get(i).getResult()).isEqualTo("맞았습니다!!");
			assertThat(correctResult.getContent().get(i).getMemoryUsage()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).getExecutionTime()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(correctResult.getContent().get(i).getLanguage()).isEqualTo("Java 11");
			assertThat(correctResult.getContent().get(i).getIsRead()).isEqualTo(false);
			assertThat(correctResult.getContent().get(i).getSolvedDateTime()).isEqualTo(
				DateFormatUtil.formatDateTime(fixedDateTime));
		}
		for (int i = 5; i < 10; i++) {
			assertThat(correctResult.getContent().get(i).getContent()).isEqualTo("content" + (i + 10));
			assertThat(correctResult.getContent().get(i).getResult()).isEqualTo("맞았습니다!!");
			assertThat(correctResult.getContent().get(i).getMemoryUsage()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).getExecutionTime()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).getNickname()).isEqualTo("nickname2");
			assertThat(correctResult.getContent().get(i).getLanguage()).isEqualTo("PyPy3");
			assertThat(correctResult.getContent().get(i).getIsRead()).isEqualTo(true);
			assertThat(correctResult.getContent().get(i).getSolvedDateTime()).isEqualTo(
				DateFormatUtil.formatDateTime(fixedDateTime));
		}
	}

	@Test
	@DisplayName("풀이 목록 조회 성공 (멤버) : 닉네임 및 언어 필터링")
	void getSolutionList_2() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> list = new ArrayList<>();
		List<SolutionComment> commentList = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();

		setTestSolutionAndCommentList(list, commentList, fixedDateTime);

		Page<Solution> solutionPage = new PageImpl<>(list.subList(10, 15), pageable, 5);

		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(group));
		when(problemRepository.findById(10L)).thenReturn(Optional.ofNullable(problem));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		when(solutionRepository.findAllFilteredSolutions(problem, "nickname1", "Java", null, pageable)).thenReturn(
			solutionPage);

		// when
		Page<GetSolutionResponse> result = solutionService.getSolutionList(user2, 10L, "nickname1", "Java", null,
			pageable);
		// then
		assertThat(result.getContent().size()).isEqualTo(5);
		assertThat(result.getTotalElements()).isEqualTo(5);
		for (int i = 0; i < 5; i++) {
			assertThat(result.getContent().get(i).getContent()).isEqualTo("content" + (i + 10));
			assertThat(result.getContent().get(i).getResult()).isEqualTo("맞았습니다!!");
			assertThat(result.getContent().get(i).getMemoryUsage()).isEqualTo(i + 10);
			assertThat(result.getContent().get(i).getExecutionTime()).isEqualTo(i + 10);
			assertThat(result.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(result.getContent().get(i).getLanguage()).isEqualTo("Java 11");
			assertThat(result.getContent().get(i).getIsRead()).isEqualTo(true);
			assertThat(result.getContent().get(i).getSolvedDateTime()).isEqualTo(
				DateFormatUtil.formatDateTime(fixedDateTime));
		}
	}

	@Test
	@DisplayName("풀이 목록 조회 실패 : 존재하지 않는 그룹")
	void getSolutionListFailed_1() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		when(problemRepository.findById(10L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> solutionService.getSolutionList(user, 10L, null, null, null, pageable))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("풀이 목록 조회 실패 : 존재하지 않는 문제")
	void getSolutionListFailed_2() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		when(problemRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> solutionService.getSolutionList(user, 10L, null, null, null, pageable))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("풀이 목록 조회 실패 : 참여하지 않은 그룹")
	void getSolutionListFailed_3() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		when(problemRepository.findById(10L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> solutionService.getSolutionList(user2, 10L, null, null, null, pageable))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("풀이 하나 조회 성공")
	void getSolution_1() {
		// given
		Solution solution = Solution.builder()
			.problem(problem)
			.content("content")
			.user(user)
			.memoryUsage(10)
			.executionTime(10)
			.result("맞았습니다!!")
			.language("Java")
			.codeLength(10)
			.solvedDateTime(LocalDateTime.now())
			.build();
		List<SolutionComment> commentList = new ArrayList<>();
		for (int j = 0; j < 5; j++)
			commentList.add(SolutionComment.builder()
				.solution(solution)
				.user(user)
				.content("content" + j)
				.isRead(false)
				.build());

		when(solutionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(solution));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(solutionCommentRepository.findAllBySolution(solution)).thenReturn(commentList);
		// when
		GetSolutionResponse response = solutionService.getSolution(user, 10L);
		// then
		assertThat(response.getContent()).isEqualTo("content");
		assertThat(response.getResult()).isEqualTo("맞았습니다!!");
		assertThat(response.getMemoryUsage()).isEqualTo(10);
		assertThat(response.getExecutionTime()).isEqualTo(10);
		assertThat(response.getNickname()).isEqualTo("nickname1");
		assertThat(response.getProfileImage()).isEqualTo("profileImage");
		assertThat(response.getLanguage()).isEqualTo("Java");
		assertThat(response.getCodeLength()).isEqualTo(10);
		assertThat(response.getCommentCount()).isEqualTo(0);
		assertThat(response.getIsRead()).isEqualTo(false);
		assertThat(response.getSolvedDateTime()).isEqualTo(DateFormatUtil.formatDateTime(LocalDateTime.now()));
	}

	@Test
	@DisplayName("풀이 하나 조회 성공 (멤버)")
	void getSolution_2() {
		// given
		Solution solution = Solution.builder()
			.problem(problem)
			.content("content")
			.user(user)
			.memoryUsage(10)
			.executionTime(10)
			.result("맞았습니다!!")
			.language("Java")
			.codeLength(10)
			.solvedDateTime(LocalDateTime.now())
			.build();
		List<SolutionComment> commentList = new ArrayList<>();
		for (int j = 0; j < 5; j++)
			commentList.add(SolutionComment.builder()
				.solution(solution)
				.user(user)
				.content("content" + j)
				.isRead(false)
				.build());

		when(solutionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(solution));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(true);
		// when
		GetSolutionResponse response = solutionService.getSolution(user2, 10L);
		// then
		assertThat(response.getContent()).isEqualTo("content");
		assertThat(response.getResult()).isEqualTo("맞았습니다!!");
		assertThat(response.getMemoryUsage()).isEqualTo(10);
		assertThat(response.getExecutionTime()).isEqualTo(10);
		assertThat(response.getNickname()).isEqualTo("nickname1");
		assertThat(response.getProfileImage()).isEqualTo("profileImage");
		assertThat(response.getLanguage()).isEqualTo("Java");
		assertThat(response.getCodeLength()).isEqualTo(10);
		assertThat(response.getCommentCount()).isEqualTo(0);
		assertThat(response.getIsRead()).isEqualTo(true);
		assertThat(response.getSolvedDateTime()).isEqualTo(DateFormatUtil.formatDateTime(LocalDateTime.now()));
	}

	@Test
	@DisplayName("풀이 하나 조회 실패 : 존재하지 않는 풀이")
	void getSolutionFailed_1() {
		// given
		when(solutionRepository.findById(anyLong())).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> solutionService.getSolution(user, 10L))
			.isInstanceOf(CannotFoundSolutionException.class)
			.hasFieldOrPropertyWithValue("errors", "존재하지 않는 풀이 입니다.");
	}

	@Test
	@DisplayName("풀이 히나 조회 실패 : 참여하지 않은 그룹")
	void getSolutionFailed_2() {
		// given
		Solution solution = Solution.builder()
			.problem(problem)
			.content("content")
			.user(user)
			.memoryUsage(10)
			.executionTime(10)
			.result("맞았습니다!!")
			.language("Java")
			.codeLength(10)
			.solvedDateTime(LocalDateTime.now())
			.build();
		when(solutionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(solution));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> solutionService.getSolution(user2, 10L))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "해당 풀이를 확인 할 권한이 없습니다.");
	}

	private void setTestSolutionAndCommentList(List<Solution> list, List<SolutionComment> commentList,
		LocalDateTime fixedDateTime) {
		for (int i = 0; i < 5; i++) {
			Solution solution = Solution.builder()
				.problem(problem)
				.content("content" + i)
				.user(user)
				.memoryUsage(i)
				.executionTime(i)
				.result("컴파일 에러")
				.language("Java 11")
				.codeLength(i)
				.solvedDateTime(fixedDateTime)
				.build();
			list.add(solution);
			for (int j = 0; j < 5; j++)
				commentList.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(true)
					.build());
		}
		for (int i = 5; i < 10; i++) {
			list.add(Solution.builder()
				.problem(problem)
				.content("content" + i)
				.user(user2)
				.memoryUsage(i)
				.executionTime(i)
				.result("컴파일 에러")
				.language("C++17")
				.codeLength(i)
				.solvedDateTime(fixedDateTime)
				.build());
		}
		for (int i = 10; i < 15; i++) {
			Solution solution = Solution.builder()
				.problem(problem)
				.content("content" + i)
				.user(user)
				.memoryUsage(i)
				.executionTime(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.codeLength(i)
				.solvedDateTime(fixedDateTime)
				.build();
			list.add(solution);
			for (int j = 5; j < 10; j++)
				commentList.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(false)
					.build());

		}
		for (int i = 15; i < 20; i++) {
			list.add(Solution.builder()
				.problem(problem)
				.content("content" + i)
				.user(user2)
				.memoryUsage(i)
				.executionTime(i)
				.result(i + "점")
				.language("PyPy3")
				.codeLength(i)
				.solvedDateTime(fixedDateTime)
				.build());
		}
	}

	@Test
	@DisplayName("풀이 추가 성공")
	void createSolution() throws NoSuchFieldException, IllegalAccessException {
		// given
		CreateSolutionRequest request = new CreateSolutionRequest(
			"bjNickname2",
			"code",
			"Java",
			"result",
			80,
			100,
			100,
			300
		);

		GroupMember member1 = GroupMember.builder()
			.studyGroup(group)
			.user(user)
			.build();
		GroupMember member2 = GroupMember.builder()
			.studyGroup(group)
			.user(user2)
			.build();
		GroupMember member3 = GroupMember.builder()
			.studyGroup(group)
			.user(user3)
			.build();

		Field memberField = GroupMember.class.getDeclaredField("id");
		memberField.setAccessible(true);
		memberField.set(member1, 10L);
		memberField.set(member2, 20L);
		memberField.set(member3, 30L);

		Problem problem = Problem.builder()
			.number(300)
			.studyGroup(group)
			.endDate(LocalDate.now().plusDays(30))
			.build();

		Solution solutionStub = mock(Solution.class);

		List<GroupMember> members = List.of(member1, member2, member3);

		List<User> sameBojNicknameUsers = List.of(user2, user3);
		when(problemRepository.findValidProblemsByNumberAndUser(any(), any(LocalDate.class),
			any(User.class))).thenReturn(List.of(problem));
		when(userRepository.findAllByBjNickname("bjNickname2")).thenReturn(sameBojNicknameUsers);
		when(solutionRepository.save(any())).thenReturn(solutionStub);
		when(solutionStub.getId()).thenReturn(123L);
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.of(member2));
		when(groupMemberRepository.findByUserAndStudyGroup(user3, group)).thenReturn(Optional.of(member3));

		when(groupMemberRepository.findAllByStudyGroup(group)).thenReturn(members);

		// when
		solutionService.createSolution(request);

		// then
		verify(solutionRepository, times(2)).save(any(Solution.class));
		verify(notificationService, times(2)).sendNotificationToMembers(any(), any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("그룹 내 진행 중인 나의 풀이 전체 조회 성공 : 문제 필터링")
	void getMySolutionsInGroupInProgress() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> inProgress = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();
		List<SolutionComment> comments = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			Solution solution = Solution.builder()
				.problem(problem)
				.user(user)
				.codeLength(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build();
			inProgress.add(solution);
			for (int j = 0; j < 5; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(false)
					.build());
			for (int j = 0; j < 5; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(true)
					.build());
		}

		Page<Solution> inProgressPages = new PageImpl<>(inProgress, pageable, 10);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(solutionRepository.findAllFilteredMySolutionsInGroup(user, group, problemNumber, null, null,
			ProgressCategory.IN_PROGRESS,
			pageable)).thenReturn(inProgressPages);
		for (int i = 0; i < 5; i++) {
			when(solutionCommentRepository.findAllBySolution(inProgressPages.getContent().get(i))).thenReturn(
				comments.subList(i * 10, i * 10 + 10));
		}
		// when
		Page<GetSolutionResponse> responses = solutionService.getMySolutionsInGroupInProgress(user, groupId,
			problemNumber, null,
			null, pageable);
		// then
		for (int i = 0; i < 5; i++) {
			assertThat(responses.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.getContent().get(i).getProblemLevel()).isEqualTo(problem.getLevel());
			assertThat(responses.getContent().get(i).getIsRead()).isEqualTo(false);
		}
	}

	@Test
	@DisplayName("그룹 내 마감된 나의 풀이 전체 조회 성공 : 문제 필터링")
	void getMySolutionsInGroupExpired() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> expired = new ArrayList<>();
		List<SolutionComment> comments = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();
		for (int i = 0; i < 5; i++) {
			Solution solution = Solution.builder()
				.problem(problem1)
				.user(user)
				.codeLength(i)
				.result("틀렸습니다")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build();
			expired.add(solution);
			for (int j = 0; j < 5; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(false)
					.build());
			for (int j = 0; j < 5; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(true)
					.build());
		}

		Page<Solution> expiredPages = new PageImpl<>(expired, pageable, 10);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(solutionRepository.findAllFilteredMySolutionsInGroup(user, group, problemNumber, null, null,
			ProgressCategory.EXPIRED,
			pageable)).thenReturn(expiredPages);
		for (int i = 0; i < 5; i++) {
			when(solutionCommentRepository.findAllBySolution(expiredPages.getContent().get(i))).thenReturn(
				comments.subList(i * 10, i * 10 + 10));
		}
		// when
		Page<GetSolutionResponse> responses = solutionService.getMySolutionsInGroupExpired(user, groupId,
			problemNumber, null,
			null, pageable);
		// then
		for (int i = 0; i < 5; i++) {
			assertThat(responses.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.getContent().get(i).getIsRead()).isEqualTo(false);
			assertThat(responses.getContent().get(i).getProblemLevel()).isEqualTo(problem1.getLevel());
		}
	}

	@Test
	@DisplayName("진행 중인 나의 풀이 전체 조회 성공")
	void getMySolutionsInProgress() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> inProgress = new ArrayList<>();
		List<SolutionComment> comments = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();

		for (int i = 0; i < 5; i++) {
			Solution solution = Solution.builder()
				.problem(problem)
				.user(user)
				.codeLength(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build();
			inProgress.add(solution);
			for (int j = 0; j < 10; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(true)
					.build());
		}

		Page<Solution> inProgressPages = new PageImpl<>(inProgress, pageable, 10);

		when(solutionRepository.findAllFilteredMySolutions(user, null, null, null,
			ProgressCategory.IN_PROGRESS, pageable)).thenReturn(inProgressPages);
		for (int i = 0; i < 5; i++) {
			when(solutionCommentRepository.findAllBySolution(inProgressPages.getContent().get(i))).thenReturn(
				comments.subList(i * 10, i * 10 + 10));
		}
		// when
		Page<GetSolutionWithGroupIdResponse> responses = solutionService.getMySolutionsInProgress(user, null, null,
			null, pageable);
		// then
		for (int i = 0; i < 5; i++) {
			assertThat(responses.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.getContent().get(i).getIsRead()).isEqualTo(true);
			assertThat(responses.getContent().get(i).getGroupId()).isEqualTo(
				problem.getStudyGroup().getId());
		}
	}

	@Test
	@DisplayName("마감 된 나의 풀이 전체 조회 성공")
	void getMySolutionsExpired() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> expired = new ArrayList<>();
		List<SolutionComment> comments = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();

		for (int i = 0; i < 5; i++) {
			Solution solution = Solution.builder()
				.problem(problem2)
				.user(user)
				.codeLength(i)
				.result("컴파일 에러")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build();
			expired.add(solution);
			for (int j = 0; j < 5; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(true)
					.build());
			for (int j = 5; j < 10; j++)
				comments.add(SolutionComment.builder()
					.solution(solution)
					.user(user)
					.content("content" + j)
					.isRead(false)
					.build());
		}

		Page<Solution> expiredPages = new PageImpl<>(expired, pageable, 10);

		when(solutionRepository.findAllFilteredMySolutions(user, null, null, null,
			ProgressCategory.EXPIRED, pageable)).thenReturn(expiredPages);
		for (int i = 0; i < 5; i++) {
			when(solutionCommentRepository.findAllBySolution(expiredPages.getContent().get(i))).thenReturn(
				comments.subList(i * 10, i * 10 + 10));
		}
		// when
		Page<GetSolutionWithGroupIdResponse> responses = solutionService.getMySolutionsExpired(user, null, null,
			null, pageable);
		// then
		for (int i = 0; i < 5; i++) {
			assertThat(responses.getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.getContent().get(i).getIsRead()).isEqualTo(false);
			assertThat(responses.getContent().get(i).getGroupId()).isEqualTo(
				problem2.getStudyGroup().getId());
		}
	}

	@Test
	@DisplayName("풀이 현황 테이블 조회 테스트")
	void getCurrentSolvingStatusesTest() {
		// given
		GroupMember member1 = GroupMember.builder().user(user).studyGroup(group).build();
		GroupMember member2 = GroupMember.builder().user(user2).studyGroup(group).build();
		List<GroupMember> members = List.of(member1, member2);

		List<Problem> problems = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Problem problem = Problem.builder()
				.title("title" + i)
				.studyGroup(group)
				.startDate(LocalDate.now())
				.build();
			problems.add(problem);

			Solution solution = Solution.builder()
				.problem(problem)
				.user(user)
				.codeLength(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.solvedDateTime(LocalDateTime.of(
					LocalDate.now(),
					LocalTime.of(10, 10)
				))
				.build();
			when(solutionRepository.findAllByUserAndProblem(user, problem)).thenReturn(List.of(solution));
		}

		when(studyGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(problemRepository.findAllInProgressProblem(group)).thenReturn(problems);
		when(groupMemberRepository.findAllByStudyGroup(group)).thenReturn(members);
		// when(solutionRepository.findAllByUserAndProblem(user, problem1)).thenReturn(solutions);
		// when(solutionRepository.findAllByUserAndProblem(user, problem2)).thenReturn(Collections.emptyList());

		// when
		List<GetCurrentSolvingStatusResponse> responses = solutionService.getCurrentSolvingStatuses(user,
			group.getId());

		// then
		assertAll(() -> {
			assertThat(responses).hasSize(2);
			GetCurrentSolvingStatusResponse response = responses.getFirst();
			assertThat(response.rank()).isEqualTo(1);
			assertThat(response.nickname()).isEqualTo("nickname1");
			assertThat(response.totalSubmissionCount()).isEqualTo(5);
			assertThat(response.totalPassedTime()).isEqualTo("50:50"); // 문제 시작일 ~ 맞은 시간 차이
			assertThat(response.problems()).hasSize(5);
			assertThat(response.problems().getFirst().solved()).isTrue();
		});
	}
}
