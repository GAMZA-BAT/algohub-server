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

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.comment.repository.CommentRepository;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
import com.gamzabat.algohub.feature.solution.exception.CannotFoundSolutionException;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.solution.service.SolutionService;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SolutionServiceTest {
	@InjectMocks
	private SolutionService solutionService;
	@Mock
	private SolutionRepository solutionRepository;
	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private CommentRepository commentRepository;
	private User user, user2;
	private Problem problem;
	private StudyGroup group;
	DateTimeFormatter formatter;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("profileImage").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("profileImage").build();
		group = StudyGroup.builder().name("name").owner(user).groupImage("imageUrl").groupCode("code").build();
		problem = Problem.builder()
			.studyGroup(group)
			.link("link")
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
	@DisplayName("풀이 목록 조회 성공 (주인) : 풀이 결과 필터링")
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
			assertThat(compileErrorResult.getContent().get(i).content()).isEqualTo("content" + i);
			assertThat(compileErrorResult.getContent().get(i).result()).isEqualTo("컴파일 에러");
			assertThat(compileErrorResult.getContent().get(i).memoryUsage()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).executionTime()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).nickname()).isEqualTo("nickname1");
			assertThat(compileErrorResult.getContent().get(i).language()).isEqualTo("Java 11");
			assertThat(compileErrorResult.getContent().get(i).solvedDateTime()).isEqualTo(
				fixedDateTime.format(formatter));
		}
		for (int i = 5; i < 10; i++) {
			assertThat(compileErrorResult.getContent().get(i).content()).isEqualTo("content" + i);
			assertThat(compileErrorResult.getContent().get(i).result()).isEqualTo("컴파일 에러");
			assertThat(compileErrorResult.getContent().get(i).memoryUsage()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).executionTime()).isEqualTo(i);
			assertThat(compileErrorResult.getContent().get(i).nickname()).isEqualTo("nickname2");
			assertThat(compileErrorResult.getContent().get(i).language()).isEqualTo("C++17");
			assertThat(compileErrorResult.getContent().get(i).solvedDateTime()).isEqualTo(
				fixedDateTime.format(formatter));
		}
		// 2) 맞았습니다!! 풀이 목록 조회
		assertThat(correctResult.getContent().size()).isEqualTo(10);
		assertThat(correctResult.getTotalElements()).isEqualTo(10);
		for (int i = 0; i < 5; i++) {
			assertThat(correctResult.getContent().get(i).content()).isEqualTo("content" + (i + 10));
			assertThat(correctResult.getContent().get(i).result()).isEqualTo("맞았습니다!!");
			assertThat(correctResult.getContent().get(i).memoryUsage()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).executionTime()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).nickname()).isEqualTo("nickname1");
			assertThat(correctResult.getContent().get(i).language()).isEqualTo("Java 11");
			assertThat(correctResult.getContent().get(i).solvedDateTime()).isEqualTo(
				fixedDateTime.format(formatter));
		}
		for (int i = 5; i < 10; i++) {
			assertThat(correctResult.getContent().get(i).content()).isEqualTo("content" + (i + 10));
			assertThat(correctResult.getContent().get(i).result()).isEqualTo((i + 10) + "점");
			assertThat(correctResult.getContent().get(i).memoryUsage()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).executionTime()).isEqualTo(i + 10);
			assertThat(correctResult.getContent().get(i).nickname()).isEqualTo("nickname2");
			assertThat(correctResult.getContent().get(i).language()).isEqualTo("PyPy3");
			assertThat(correctResult.getContent().get(i).solvedDateTime()).isEqualTo(
				fixedDateTime.format(formatter));
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
			assertThat(result.getContent().get(i).content()).isEqualTo("content" + (i + 10));
			assertThat(result.getContent().get(i).result()).isEqualTo("맞았습니다!!");
			assertThat(result.getContent().get(i).memoryUsage()).isEqualTo(i + 10);
			assertThat(result.getContent().get(i).executionTime()).isEqualTo(i + 10);
			assertThat(result.getContent().get(i).nickname()).isEqualTo("nickname1");
			assertThat(result.getContent().get(i).language()).isEqualTo("Java 11");
			assertThat(result.getContent().get(i).solvedDateTime()).isEqualTo(
				fixedDateTime.format(formatter));
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
	@DisplayName("풀이 하나 조회 성공 (주인)")
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
		// when
		GetSolutionResponse response = solutionService.getSolution(user, 10L);
		// then
		assertThat(response.content()).isEqualTo("content");
		assertThat(response.result()).isEqualTo("맞았습니다!!");
		assertThat(response.memoryUsage()).isEqualTo(10);
		assertThat(response.executionTime()).isEqualTo(10);
		assertThat(response.nickname()).isEqualTo("nickname1");
		assertThat(response.profileImage()).isEqualTo("profileImage");
		assertThat(response.language()).isEqualTo("Java");
		assertThat(response.codeLength()).isEqualTo(10);
		assertThat(response.commentCount()).isEqualTo(0);
		assertThat(response.solvedDateTime()).isEqualTo(LocalDateTime.now().format(formatter));
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
		assertThat(response.content()).isEqualTo("content");
		assertThat(response.result()).isEqualTo("맞았습니다!!");
		assertThat(response.memoryUsage()).isEqualTo(10);
		assertThat(response.executionTime()).isEqualTo(10);
		assertThat(response.nickname()).isEqualTo("nickname1");
		assertThat(response.profileImage()).isEqualTo("profileImage");
		assertThat(response.language()).isEqualTo("Java");
		assertThat(response.codeLength()).isEqualTo(10);
		assertThat(response.commentCount()).isEqualTo(0);
		assertThat(response.solvedDateTime()).isEqualTo(LocalDateTime.now().format(formatter));
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
}