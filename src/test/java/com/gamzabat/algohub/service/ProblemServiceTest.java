package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.notification.domain.Notification;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemListsResponse;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.exception.SolvedAcApiErrorException;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {
	@InjectMocks
	private ProblemService problemService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private StudyGroupRepository groupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private SolutionRepository solutionRepository;
	@Mock
	private NotificationRepository notificationRepository;
	@Mock
	private RestTemplate restTemplate;

	private User user;
	private User user2;
	private User user3;
	private User user4;
	private StudyGroup group;
	private GroupMember groupMember1;
	private GroupMember groupMember3;
	private GroupMember groupMember4;

	private Problem problem;

	@Captor
	private ArgumentCaptor<Problem> problemCaptor;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user3 = User.builder().email("email3").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user4 = User.builder().email("email4").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		group = StudyGroup.builder().name("name").groupImage("imageUrl").groupCode("code").build();
		groupMember1 = GroupMember.builder().role(RoleOfGroupMember.OWNER).studyGroup(group).user(user).build();
		groupMember3 = GroupMember.builder().role(RoleOfGroupMember.ADMIN).studyGroup(group).user(user3).build();
		groupMember4 = GroupMember.builder().role(RoleOfGroupMember.PARTICIPANT).studyGroup(group).user(user4).build();

		problem = Problem.builder()
			.studyGroup(group)
			.link("link")
			.startDate(LocalDate.now().plusDays(3))
			.endDate(LocalDate.now().plusDays(10))
			.build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group, 10L);

		Field problemId = Problem.class.getDeclaredField("id");
		problemId.setAccessible(true);
		problemId.set(problem, 20L);
	}

	@Test
	@DisplayName("문제 생성 성공")
	void createProblem_Success() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("https://www.acmicpc.net/problem/1000")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		String apiResult = "[{\"titleKo\":\"A+B\",\"level\":1}]";
		ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResult, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when
		problemService.createProblem(user, request);
		// then
		verify(problemRepository, times(1)).save(problemCaptor.capture());
		Problem result = problemCaptor.getValue();
		assertThat(result.getStudyGroup()).isEqualTo(group);
		assertThat(result.getLink()).isEqualTo("https://www.acmicpc.net/problem/1000");
		assertThat(result.getNumber()).isEqualTo(1000);
		assertThat(result.getTitle()).isEqualTo("A+B");
		assertThat(result.getLevel()).isEqualTo(1);
		assertThat(result.getStartDate()).isEqualTo(LocalDate.now().minusDays(7));
		assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
		verify(notificationService, times(1)).sendList(any(), any(), any(), any());
	}

	@Test
	@DisplayName("문제 생성 성공 : 부방장일 때")
	void createProblem_SuccessByADMIN() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("https://www.acmicpc.net/problem/1000")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user3, group)).thenReturn(Optional.of(groupMember3));
		String apiResult = "[{\"titleKo\":\"A+B\",\"level\":1}]";
		ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResult, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
		// when
		problemService.createProblem(user3, request);
		// then
		verify(problemRepository, times(1)).save(problemCaptor.capture());
		Problem result = problemCaptor.getValue();
		assertThat(result.getStudyGroup()).isEqualTo(group);
		assertThat(result.getLink()).isEqualTo("https://www.acmicpc.net/problem/1000");
		assertThat(result.getNumber()).isEqualTo(1000);
		assertThat(result.getTitle()).isEqualTo("A+B");
		assertThat(result.getLevel()).isEqualTo(1);
		assertThat(result.getStartDate()).isEqualTo(LocalDate.now().minusDays(7));
		assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
		verify(notificationService, times(1)).sendList(any(), any(), any(), any());
	}

	@Test
	@DisplayName("문제 생성 실패 : 존재하지 않는 그룹")
	void createProblemFailed_1() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("link")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 생성 실패 : 권한 없음 // 그룹원이 아닌 경우")
	void createProblemFailed_2() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("link")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user2, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 생성 실패 : 권한 없음 // Role이 PARTICIPANT인 경우")
	void createProblemFailed_4() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("link")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user4, group)).thenReturn(Optional.of(groupMember4));
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user4, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "문제 생성 권한이 없습니다. 방장, 부방장일 경우에만 생성이 가능합니다.");
	}

	@Test
	@DisplayName("문제 생성 실패 : 백준에 유효하지 않은 문제")
	void createProblemFailed_5() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("https://www.acmicpc.net/problem/00")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		String apiResult = "[]";
		ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResult, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user, request))
			.isInstanceOf(SolvedAcApiErrorException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "백준에 유효하지 않은 문제입니다.");
	}

	@Test
	@DisplayName("문제 생성 실패 : solved.ac API의 잘못된 response")
	void createProblemFailed_6() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("https://www.acmicpc.net/problem/00")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		String apiResult = "{\"titleKo\":\"A+B\",\"level\":1}";
		ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResult, HttpStatus.OK);
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user, request))
			.isInstanceOf(SolvedAcApiErrorException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.SERVICE_UNAVAILABLE.value())
			.hasFieldOrPropertyWithValue("error", "solved.ac API로부터 예상치 못한 응답을 받았습니다.");
	}

	@Test
	@DisplayName("문제 생성 성공, 알림 전송 실패")
	void createProblemSuccess_NotificationFailed() {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("https://www.acmicpc.net/problem/1000")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		String apiResult = "[{\"titleKo\":\"A+B\",\"level\":1}]";
		ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResult, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		doThrow(new RuntimeException()).when(notificationService).sendList(any(), any(), any(), any());
		// when
		problemService.createProblem(user, request);
		// then
		verify(problemRepository, times(1)).save(any(Problem.class));
		verify(notificationService, times(1)).sendList(any(), any(), any(), any());
		verify(notificationRepository, never()).save(any(Notification.class));
	}

	@Test
	@DisplayName("문제 정보 수정 성공")
	void editProblem() {
		// given
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when
		problemService.editProblem(user, request);
		// then
		assertThat(problem.getStartDate()).isEqualTo(request.startDate());
		assertThat(problem.getEndDate()).isEqualTo(request.endDate());
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 존재하지 않는 그룹")
	void editProblemFailed_1() {
		// given
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 마감 기한 수정 실패 : 존재하지 않는 문제")
	void editProblemFailed_2() {
		// given
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user, request))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 권한 없음 // 그룹원이 아닌경우")
	void editProblemFailed_3() {
		// given
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user2, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 권한 없음 // Role이 PARTICIPAN인 경우")
	void editProblemFailed_4() {
		// given
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user4, group)).thenReturn(Optional.of(groupMember4));
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user4, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "문제 수정 권한이 없습니다. 방장, 부방장일 경우에만 수정이 가능합니다.");
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 이미 진행 중인 문제인데 시작날짜 수정을 요청하는 경우")
	void editProblemFailed_5() {
		// given
		Problem problem = Problem.builder()
			.studyGroup(group)
			.link("link")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(10))
			.build();
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now().plusDays(1))
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user, request))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "문제 시작 날짜 수정이 불가합니다. : 이미 진행 중인 문제입니다.");
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 문제 시작 날짜를 오늘 이전의 날짜로 요청한 경우")
	void editProblemFailed_6() {
		// given
		Problem problem = Problem.builder()
			.studyGroup(group)
			.link("link")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(10))
			.build();
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now().minusDays(3))
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user, request))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "문제 시작 날짜는 오늘 이전의 날짜로 수정할 수 없습니다.");
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 문제 마감 날짜를 오늘 이전의 날짜로 요청한 경우")
	void editProblemFailed_7() {
		// given
		Problem problem = Problem.builder()
			.studyGroup(group)
			.link("link")
			.startDate(LocalDate.now().minusDays(10))
			.endDate(LocalDate.now())
			.build();
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now().minusDays(10))
			.endDate(LocalDate.now().minusDays(2))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when, then
		assertThatThrownBy(() -> problemService.editProblem(user, request))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "문제 마감 날짜는 오늘 이전의 날짜로 수정할 수 없습니다.");
	}

	@Test
	@DisplayName("문제 목록 조회 성공")
	void getProblemList() throws NoSuchFieldException, IllegalAccessException {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		Field problemField = Problem.class.getDeclaredField("id");
		problemField.setAccessible(true);

		List<Problem> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Problem problem = Problem.builder()
				.studyGroup(group)
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(i + 1))
				.link("https://www.acmicpc.net/problem/" + i)
				.title("title" + i)
				.build();
			list.add(problem);
			problemField.set(problem, (long)i);
		}
		for (int i = 10; i < 20; i++) {
			Problem problem = Problem.builder()
				.studyGroup(group)
				.startDate(LocalDate.now().minusDays(i + 30))
				.endDate(LocalDate.now().minusDays(i))
				.link("https://www.acmicpc.net/problem/" + i)
				.title("title" + i)
				.build();
			list.add(problem);
			problemField.set(problem, (long)i);
		}

		Page<Problem> problemPage = new PageImpl<>(list.subList(0, 20), pageable, list.size());
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(problemRepository.findAllByStudyGroup(eq(group), any(Pageable.class))).thenReturn(problemPage);
		when(groupMemberRepository.existsByUserAndStudyGroup(user, group)).thenReturn(true);
		// 각 문제 ID에 대한 stub 설정
		when(solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(anyLong(),
			anyString())).thenReturn(8);
		when(solutionRepository.countDistinctUsersByProblemId(anyLong())).thenReturn(10);
		// when
		GetProblemListsResponse result = problemService.getProblemList(user, 10L, pageable);
		// then
		List<GetProblemResponse> inProgress = result.getInProgressProblems();
		List<GetProblemResponse> expired = result.getExpiredProblems();
		assertThat(inProgress.size()).isEqualTo(10);
		assertThat(expired.size()).isEqualTo(10);
		for (int i = 0; i < 10; i++) {
			assertThat(inProgress.get(i).getProblemId()).isEqualTo(i);
			assertThat(inProgress.get(i).getLink()).isEqualTo("https://www.acmicpc.net/problem/" + i);
			assertThat(inProgress.get(i).getTitle()).isEqualTo("title" + i);
			assertThat(inProgress.get(i).getEndDate()).isEqualTo(LocalDate.now().plusDays(i + 1));
		}
		for (int i = 10; i < 20; i++) {
			assertThat(expired.get(i - 10).getProblemId()).isEqualTo(i);
			assertThat(expired.get(i - 10).getLink()).isEqualTo("https://www.acmicpc.net/problem/" + i);
			assertThat(expired.get(i - 10).getTitle()).isEqualTo("title" + i);
			assertThat(expired.get(i - 10).getEndDate()).isEqualTo(LocalDate.now().minusDays(i));
		}
	}

	@Test
	@DisplayName("문제 목록 조회 실패 : 존재하지 않는 그룹")
	void getProblemListFailed_1() {
		// given
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.getProblemList(user, 10L, any(Pageable.class)))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 목록 조회 실패 : 문제 조회 권한 없음 // 아예 그룹원이 아닌 경우")
	void getProblemListFailed_2() {
		// given
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, group)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> problemService.getProblemList(user2, 10L, any(Pageable.class)))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "문제를 조회할 권한이 없습니다.");
	}

	@Test
	@DisplayName("문제 삭제 성공")
	void deleteProblem() {
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		// when
		problemService.deleteProblem(user, 20L);
		// then
		verify(problemRepository, times(1)).delete(problem);
	}

	@Test
	@DisplayName("문제 삭제 실패 : 존재하지 않는 문제")
	void deleteProblemFailed_1() {
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.deleteProblem(user, 20L))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("문제 삭제 실패 : 존재하지 않는 그룹")
	void deleteProblemFailed_2() {
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.deleteProblem(user, 20L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 삭제 실패 : 권한 없음")
	void deleteProblemFailed_3() {
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user4, group)).thenReturn(Optional.ofNullable(groupMember4));
		// when, then
		assertThatThrownBy(() -> problemService.deleteProblem(user4, 20L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "문제 삭제 권한이 없습니다. 방장, 부방장일 경우에만 삭제가 가능합니다.");
	}

	@Test
	@DisplayName("예정 문제 조회 성공 : 방장")
	void getQueuedProblemSuccess_1() throws NoSuchFieldException, IllegalAccessException {

		//given
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		Field problemField = Problem.class.getDeclaredField("id");
		problemField.setAccessible(true);

		List<Problem> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Problem problem = Problem.builder()
				.studyGroup(group)
				.startDate(LocalDate.now().plusDays(1))
				.endDate(LocalDate.now().plusDays(i + 1))
				.link("https://www.acmicpc.net/problem/" + i)
				.title("title" + i)
				.build();
			list.add(problem);
			problemField.set(problem, (long)i);
		}
		when(problemRepository.findAllByStudyGroupAndStartDateAfter(group, LocalDate.now())).thenReturn(list);
		when(groupMemberRepository.findByUserAndStudyGroup(user, group)).thenReturn(Optional.ofNullable(groupMember1));
		//when
		List<GetProblemResponse> result = problemService.getQueuedProblemList(user, group.getId());

		//then
		assertThat(result.size()).isEqualTo(10);
		for (int i = 0; i < 10; i++) {
			assertThat(result.get(i).getProblemId()).isEqualTo(i);
			assertThat(result.get(i).getLink()).isEqualTo("https://www.acmicpc.net/problem/" + i);
			assertThat(result.get(i).getTitle()).isEqualTo("title" + i);
			assertThat(result.get(i).getStartDate()).isEqualTo(LocalDate.now().plusDays(1));
			assertThat(result.get(i).getEndDate()).isEqualTo(LocalDate.now().plusDays(i + 1));
		}
	}

	@Test
	@DisplayName("예정 문제 조회 성공 : 부방장")
	void getQueuedProblemSuccess_2() throws NoSuchFieldException, IllegalAccessException {

		//given
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		Field problemField = Problem.class.getDeclaredField("id");
		problemField.setAccessible(true);

		List<Problem> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Problem problem = Problem.builder()
				.studyGroup(group)
				.startDate(LocalDate.now().plusDays(1))
				.endDate(LocalDate.now().plusDays(i + 1))
				.link("https://www.acmicpc.net/problem/" + i)
				.title("title" + i)
				.build();
			list.add(problem);
			problemField.set(problem, (long)i);
		}
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user3, group)).thenReturn(Optional.of(groupMember3));
		when(problemRepository.findAllByStudyGroupAndStartDateAfter(group, LocalDate.now())).thenReturn(list);

		//when
		List<GetProblemResponse> result = problemService.getQueuedProblemList(user3, group.getId());

		//then
		assertThat(result.size()).isEqualTo(10);
		for (int i = 0; i < 10; i++) {
			assertThat(result.get(i).getProblemId()).isEqualTo(i);
			assertThat(result.get(i).getLink()).isEqualTo("https://www.acmicpc.net/problem/" + i);
			assertThat(result.get(i).getTitle()).isEqualTo("title" + i);
			assertThat(result.get(i).getStartDate()).isEqualTo(LocalDate.now().plusDays(1));
			assertThat(result.get(i).getEndDate()).isEqualTo(LocalDate.now().plusDays(i + 1));
		}
	}

	@Test
	@DisplayName("예정 문제 조회 실패 : 그룹을 찾지 못함")
	void getQueuedProblemListFailed_2() {
		//given
		when(groupRepository.findById(20L)).thenReturn(Optional.empty());

		//whe
		//then
		assertThatThrownBy(() -> problemService.getQueuedProblemList(user2, 20L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("예정 문제 조회 실패 : 그룹원이 아님")
	void getQueuedProblemListFailed_3() {
		//given
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, group)).thenReturn(Optional.empty());

		//when
		//then
		assertThatThrownBy(() -> problemService.getQueuedProblemList(user2, 10L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("예정 문제 조회 실패 : 권한 없음")
	void getQueuedProblemListFailed_4() {
		//given
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupMemberRepository.findByUserAndStudyGroup(user4, group)).thenReturn(Optional.of(groupMember4));

		//when
		//then
		assertThatThrownBy(() -> problemService.getQueuedProblemList(user4, 10L))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "예정 문제를 조회할 권한이 없습니다. : 그룹의 방장과 부방장만 볼 수 있습니다.");
	}

	@Test
	@DisplayName("문제 시작 날짜가 오늘일 시 그룹 멤버들에게 알림 전송")
	void sendProblemStartedNotification() {
		// given
		StudyGroup group2 = StudyGroup.builder().name("group2").build();
		User user11 = User.builder().email("email1").build();
		GroupMember groupMember11 = GroupMember.builder().user(user11).studyGroup(group2).build();

		List<GroupMember> group1Members = List.of(groupMember1, groupMember3, groupMember4);
		List<GroupMember> group2Members = List.of(groupMember11);

		List<Problem> problems = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			problems.add(Problem.builder()
				.studyGroup(group)
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(30))
				.title("started problem")
				.build());
		}
		for (int i = 5; i < 10; i++) {
			problems.add(Problem.builder()
				.studyGroup(group2)
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(30))
				.title("started problem")
				.build());
		}

		when(problemRepository.findAllByStartDate(LocalDate.now())).thenReturn(problems);
		when(groupMemberRepository.findAllByStudyGroup(group)).thenReturn(group1Members);
		when(groupMemberRepository.findAllByStudyGroup(group2)).thenReturn(group2Members);
		// when
		problemService.dailyProblemScheduler();
		// then
		verify(notificationService, times(10)).sendList(anyList(), anyString(), any(StudyGroup.class), eq(null));
	}

}