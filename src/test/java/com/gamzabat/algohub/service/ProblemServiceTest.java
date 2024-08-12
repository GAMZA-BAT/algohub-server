package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gamzabat.algohub.feature.notification.domain.Notification;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.dto.GetProblemListsResponse;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
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

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;

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

	private User user;
	private User user2;
	private StudyGroup group;
	private Problem problem;
	@Captor
	private ArgumentCaptor<Problem> problemCaptor;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		group = StudyGroup.builder().name("name").owner(user).groupImage("imageUrl").groupCode("code").build();
		problem = Problem.builder().studyGroup(group).link("link").startDate(LocalDate.now().minusDays(7)).endDate(LocalDate.now()).build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user,1L);
		userField.set(user2,2L);

		Field groupId = StudyGroup.class.getDeclaredField("id");
		groupId.setAccessible(true);
		groupId.set(group,10L);

		Field problemId = Problem.class.getDeclaredField("id");
		problemId.setAccessible(true);
		problemId.set(problem,20L);
	}

	 @Test
	 @DisplayName("문제 생성 성공")
	 void createProblem() {
	 	// given
	 	CreateProblemRequest request = CreateProblemRequest.builder()
	 		.groupId(10L)
	 		.link("https://www.acmicpc.net/problem/1000")
	 		.startDate(LocalDate.now().minusDays(7))
	 		.endDate(LocalDate.now())
	 		.build();
	 	when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
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
		 verify(notificationService, times(1)).sendList(any(),any(),any(),any());
	}

	@Test
	@DisplayName("문제 생성 실패 : 존재하지 않는 그룹")
	void createProblemFailed_1(){
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("link")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user,request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 생성 실패 : 권한 없음")
	void createProblemFailed_2(){
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.groupId(10L)
			.link("link")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when, then
		assertThatThrownBy(() -> problemService.createProblem(user2,request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","문제에 대한 권한이 없습니다. : create");
	}

	@Test
	@DisplayName("문제 생성 성공, 알림 전송 실패")
	void createProblemSuccess_NotificationFailed(){
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
				.groupId(10L)
				.link("https://www.acmicpc.net/problem/1000")
				.startDate(LocalDate.now().minusDays(7))
				.endDate(LocalDate.now())
				.build();
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		doThrow(new RuntimeException()).when(notificationService).sendList(any(),any(),any(),any());
		// when
		problemService.createProblem(user, request);
		// then
		verify(problemRepository,times(1)).save(any(Problem.class));
		verify(notificationService, times(1)).sendList(any(),any(),any(),any());
		verify(notificationRepository, never()).save(any(Notification.class));
	}

	@Test
	@DisplayName("문제 정보 수정 성공")
	void editProblem(){
		// given
		EditProblemRequest request = EditProblemRequest.builder()
			.problemId(20L)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(7))
			.build();
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when
		problemService.editProblem(user,request);
		// then
		assertThat(problem.getStartDate()).isEqualTo(request.startDate());
		assertThat(problem.getEndDate()).isEqualTo(request.endDate());
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 존재하지 않는 그룹")
	void editProblemFailed_1(){
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
			.hasFieldOrPropertyWithValue("code",HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 마감 기한 수정 실패 : 존재하지 않는 문제")
	void editProblemFailed_2(){
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
			.hasFieldOrPropertyWithValue("code",HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("문제 정보 수정 실패 : 권한 없음")
	void editProblemFailed_3(){
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
			.hasFieldOrPropertyWithValue("code",HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","문제에 대한 권한이 없습니다. : edit");
	}

	@Test
	@DisplayName("문제 목록 조회 성공")
	void getProblemList() throws NoSuchFieldException, IllegalAccessException {
		// given
		Pageable pageable = PageRequest.of(0,20);
		Field problemField = Problem.class.getDeclaredField("id");
		problemField.setAccessible(true);

		List<Problem> list = new ArrayList<>();
		for(int i=0; i<10; i++){
			Problem problem = Problem.builder()
				.studyGroup(group)
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(i+1))
				.link("https://www.acmicpc.net/problem/"+i)
				.title("title"+i)
				.build();
			list.add(problem);
			problemField.set(problem,(long)i);
		}
		for(int i=10; i<20; i++){
			Problem problem = Problem.builder()
					.studyGroup(group)
					.startDate(LocalDate.now().minusDays(i+30))
					.endDate(LocalDate.now().minusDays(i))
					.link("https://www.acmicpc.net/problem/"+i)
					.title("title"+i)
					.build();
			list.add(problem);
			problemField.set(problem,(long)i);
		}

		Page<Problem> problemPage = new PageImpl<>(list.subList(0,20),pageable,list.size());
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(problemRepository.findAllByStudyGroup(eq(group),any(Pageable.class))).thenReturn(problemPage);
		// 각 문제 ID에 대한 stub 설정
		when(solutionRepository.countDistinctUsersWithCorrectSolutionsByProblemId(anyLong())).thenReturn(8);
		when(solutionRepository.countDistinctUsersByProblemId(anyLong())).thenReturn(10);
		// when
		GetProblemListsResponse result = problemService.getProblemList(user, 10L, pageable);
		// then
		List<GetProblemResponse> inProgress = result.getInProgressProblems();
		List<GetProblemResponse> expired = result.getExpiredProblems();
		assertThat(inProgress.size()).isEqualTo(10);
		assertThat(expired.size()).isEqualTo(10);
		for(int i=0; i<10; i++){
			assertThat(inProgress.get(i).getProblemId()).isEqualTo(i);
			assertThat(inProgress.get(i).getLink()).isEqualTo("https://www.acmicpc.net/problem/"+i);
			assertThat(inProgress.get(i).getTitle()).isEqualTo("title"+i);
			assertThat(inProgress.get(i).getEndDate()).isEqualTo(LocalDate.now().plusDays(i+1));
		}
		for(int i=10; i<20; i++){
			assertThat(expired.get(i-10).getProblemId()).isEqualTo(i);
			assertThat(expired.get(i-10).getLink()).isEqualTo("https://www.acmicpc.net/problem/"+i);
			assertThat(expired.get(i-10).getTitle()).isEqualTo("title"+i);
			assertThat(expired.get(i-10).getEndDate()).isEqualTo(LocalDate.now().minusDays(i));
		}
	}

	@Test
	@DisplayName("문제 목록 조회 실패 : 존재하지 않는 그룹")
	void getProblemListFailed_1(){
		// given
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.getProblemList(user, 10L,any(Pageable.class)))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 목록 조회 실패 : 문제 조회 권한 없음")
	void getProblemListFailed_2(){
		// given
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,group)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> problemService.getProblemList(user2, 10L,any(Pageable.class)))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","문제를 조회할 권한이 없습니다.");
	}

	@Test
	@DisplayName("문제 삭제 성공")
	void deleteProblem(){
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when
		problemService.deleteProblem(user,20L);
		// then
		verify(problemRepository,times(1)).delete(problem);
	}

	@Test
	@DisplayName("문제 삭제 실패 : 존재하지 않는 문제")
	void deleteProblemFailed_1(){
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.deleteProblem(user,20L))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("문제 삭제 실패 : 존재하지 않는 그룹")
	void deleteProblemFailed_2(){
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> problemService.deleteProblem(user,20L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("문제 삭제 실패 : 권한 없음")
	void deleteProblemFailed_3(){
		// given
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(groupRepository.findById(10L)).thenReturn(Optional.ofNullable(group));
		// when, then
		assertThatThrownBy(() -> problemService.deleteProblem(user2,20L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","문제에 대한 권한이 없습니다. : delete");
	}

}