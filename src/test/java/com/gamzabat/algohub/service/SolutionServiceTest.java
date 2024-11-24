package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.gamzabat.algohub.feature.solution.dto.CreateSolutionRequest;
import com.gamzabat.algohub.feature.solution.dto.GetMySolutionListResponse;
import com.gamzabat.algohub.feature.solution.dto.GetMySolutionListWithGroupIdResponse;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
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
	private User user, user2;
	private Problem problem, problem1, problem2;
	private StudyGroup group, group1;
	private Long groupId = 30L;
	private Integer problemNumber = 1010;
	DateTimeFormatter formatter;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		user = User.builder().email("email1").password("password").nickname("nickname1").bjNickname("bjNickname1")
			.role(Role.USER).profileImage("profileImage").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2").bjNickname("bjNickname2")
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

		LocalDateTime fixedDateTime = LocalDateTime.now();

		setTestSolutionList(list, fixedDateTime);

		Page<Solution> compileErrorPage = new PageImpl<>(list.subList(0, 10), pageable, 10);
		Page<Solution> correctPage = new PageImpl<>(list.subList(10, 20), pageable, 10);
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(group));
		when(problemRepository.findById(10L)).thenReturn(Optional.ofNullable(problem));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(solutionRepository.findAllFilteredSolutions(problem, null, null, "컴파일 에러", pageable)).thenReturn(
			compileErrorPage);
		when(solutionRepository.findAllFilteredSolutions(problem, null, null, "맞았습니다!!", pageable)).thenReturn(
			correctPage);
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

		LocalDateTime fixedDateTime = LocalDateTime.now();

		setTestSolutionList(list, fixedDateTime);

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
		when(solutionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(solution));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
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

	private void setTestSolutionList(List<Solution> list, LocalDateTime fixedDateTime) {
		for (int i = 0; i < 5; i++) {
			list.add(Solution.builder()
				.problem(problem)
				.content("content" + i)
				.user(user)
				.memoryUsage(i)
				.executionTime(i)
				.result("컴파일 에러")
				.language("Java 11")
				.codeLength(i)
				.solvedDateTime(fixedDateTime)
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
			list.add(Solution.builder()
				.problem(problem)
				.content("content" + i)
				.user(user)
				.memoryUsage(i)
				.executionTime(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.codeLength(i)
				.solvedDateTime(fixedDateTime)
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
	void createSolution() {
		// given
		CreateSolutionRequest request = new CreateSolutionRequest(
			"bjNickname",
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

		Problem problem = Problem.builder()
			.number(300)
			.studyGroup(group)
			.endDate(LocalDate.now().plusDays(30))
			.build();

		List<GroupMember> members = List.of(member1, member2);
		when(problemRepository.findAllByNumber(300)).thenReturn(List.of(problem));
		when(userRepository.findByBjNickname("bjNickname")).thenReturn(Optional.of(user));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.of(member1));
		when(groupMemberRepository.findAllByStudyGroup(group)).thenReturn(members);

		// when
		solutionService.createSolution(request);

		// then
		verify(solutionRepository, times(1)).save(any(Solution.class));
		verify(notificationService, times(1)).sendNotificationToMembers(any(), any(), any(), any());
	}

	@Test
	@DisplayName("그룹 내 나의 풀이 전체 조회 성공 : 문제 필터링")
	void getMySolutionsInGroup() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> inProgress = new ArrayList<>();
		List<Solution> expired = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();

		for (int i = 0; i < 5; i++) {
			inProgress.add(Solution.builder()
				.problem(problem)
				.user(user)
				.codeLength(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build());
		}
		for (int i = 0; i < 5; i++) {
			expired.add(Solution.builder()
				.problem(problem1)
				.user(user)
				.codeLength(i)
				.result("틀렸습니다")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build());
		}

		Page<Solution> inProgressPages = new PageImpl<>(inProgress, pageable, 10);
		Page<Solution> expiredPages = new PageImpl<>(expired, pageable, 10);
		when(studyGroupRepository.findById(groupId)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		when(solutionRepository.findAllFilteredMySolutionsInGroup(user, group, problemNumber, null, null,
			ProgressCategory.IN_PROGRESS,
			pageable)).thenReturn(inProgressPages);
		when(solutionRepository.findAllFilteredMySolutionsInGroup(user, group, problemNumber, null, null,
			ProgressCategory.EXPIRED,
			pageable)).thenReturn(expiredPages);
		// when
		GetMySolutionListResponse responses = solutionService.getMySolutionsInGroup(user, groupId, problemNumber, null,
			null, pageable);
		// then
		for (int i = 0; i < 5; i++) {
			assertThat(responses.inProgress().getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.inProgress().getContent().get(i).getProblemLevel()).isEqualTo(problem.getLevel());
		}
		for (int i = 0; i < 5; i++) {
			assertThat(responses.expired().getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.expired().getContent().get(i).getProblemLevel()).isEqualTo(problem1.getLevel());
		}
	}

	@Test
	@DisplayName("나의 풀이 전체 조회 성공")
	void getMySolutions() {
		// given
		Pageable pageable = PageRequest.of(0, 10);
		List<Solution> inProgress = new ArrayList<>();
		List<Solution> expired = new ArrayList<>();
		LocalDateTime fixedDateTime = LocalDateTime.now();

		for (int i = 0; i < 5; i++) {
			inProgress.add(Solution.builder()
				.problem(problem)
				.user(user)
				.codeLength(i)
				.result("맞았습니다!!")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build());
		}
		for (int i = 0; i < 5; i++) {
			expired.add(Solution.builder()
				.problem(problem2)
				.user(user)
				.codeLength(i)
				.result("컴파일 에러")
				.language("Java 11")
				.solvedDateTime(fixedDateTime)
				.build());
		}

		Page<Solution> inProgressPages = new PageImpl<>(inProgress, pageable, 10);
		Page<Solution> expiredPages = new PageImpl<>(expired, pageable, 10);

		when(solutionRepository.findAllFilteredMySolutions(user, null, null, null,
			ProgressCategory.IN_PROGRESS, pageable)).thenReturn(inProgressPages);
		when(solutionRepository.findAllFilteredMySolutions(user, null, null, null,
			ProgressCategory.EXPIRED, pageable)).thenReturn(expiredPages);
		// when
		GetMySolutionListWithGroupIdResponse responses = solutionService.getMySolutions(user, null, null,
			null, pageable);
		// then
		for (int i = 0; i < 5; i++) {
			assertThat(responses.inProgress().getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.inProgress().getContent().get(i).getGroupId()).isEqualTo(
				problem.getStudyGroup().getId());
		}
		for (int i = 0; i < 5; i++) {
			assertThat(responses.expired().getContent().get(i).getNickname()).isEqualTo("nickname1");
			assertThat(responses.expired().getContent().get(i).getGroupId()).isEqualTo(
				problem2.getStudyGroup().getId());
		}
	}
}