package com.gamzabat.algohub.feature.problem.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.config.SpringSecurityConfig;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.exception.SolvedAcApiErrorException;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@WebMvcTest(ProblemController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class ProblemControllerTest {
	private final String token = "token";
	private final Long groupId = 1L;
	private final Long problemId = 10L;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private ProblemService problemService;
	@MockBean
	private ProblemRepository problemRepository;
	@MockBean
	private SolutionRepository solutionRepository;
	@MockBean
	private StudyGroupRepository studyGroupRepository;
	@MockBean
	private GroupMemberRepository groupMemberRepository;
	@MockBean
	private TokenProvider tokenProvider;
	@MockBean
	private UserRepository userRepository;
	private User user;

	@BeforeEach
	void setUp() {
		user = User.builder().email("email").password("password").build();
		when(tokenProvider.getUserEmail(token)).thenReturn("email");
		when(userRepository.findByEmail("email")).thenReturn(Optional.ofNullable(user));
	}

	@Test
	@DisplayName("문제 생성 성공")
	void createProblem() throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.link("link")
			.startDate(LocalDate.now().minusDays(7))
			.endDate(LocalDate.now())
			.build();
		doNothing().when(problemService).createProblem(any(User.class), anyLong(), any(CreateProblemRequest.class));
		// when, then
		mockMvc.perform(post("/api/groups/{groupId}/problems", groupId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
		verify(problemService, times(1)).createProblem(user, groupId, request);
	}

	@ParameterizedTest
	@CsvSource(value = {
		"'','2024-07-10','2024-07-18',link : 문제 링크는 필수 입력 입니다.",
		"link, null, '2024-07-18', startDate : 시작 날짜는 필수 입력 입니다.",
		"link, '2024-07-18', null, endDate : 마감 날짜는 필수 입력 입니다."
	}, nullValues = "null")
	@DisplayName("문제 생성 실패 : 잘못된 요청")
	void createProblemFailed_1(String link, LocalDate startDate, LocalDate endDate,
		String exceptionMessage) throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.link(link)
			.startDate(startDate)
			.endDate(endDate)
			.build();
		// when, then
		mockMvc.perform(post("/api/groups/{groupId}/problems", groupId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("문제 생성 요청이 올바르지 않습니다."))
			.andExpect(jsonPath("$.messages", hasItems(exceptionMessage)));
	}

	@Test
	@DisplayName("문제 생성 실패 : 권한 없음")
	void createProblemFailed_2() throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.link("link")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now())
			.build();
		doThrow(new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
			"문제 생성 권한이 없습니다. 방장, 부방장일 경우에만 생성이 가능합니다.")).when(
			problemService).createProblem(user, groupId, request);
		// when, then
		mockMvc.perform(post("/api/groups/{groupId}/problems", groupId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제 생성 권한이 없습니다. 방장, 부방장일 경우에만 생성이 가능합니다."));
		verify(problemService, times(1)).createProblem(user, groupId, request);
	}

	@Test
	@DisplayName("문제 생성 실패 : 백준에 유효하지 않은 문제")
	void createProblemFailed_3() throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.link("link")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now())
			.build();
		doThrow(new SolvedAcApiErrorException(HttpStatus.BAD_REQUEST.value(), "백준에 유효하지 않은 문제입니다.")).when(
			problemService).createProblem(user, groupId, request);
		// when, then
		mockMvc.perform(post("/api/groups/{groupId}/problems", groupId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("백준에 유효하지 않은 문제입니다."));
		verify(problemService, times(1)).createProblem(user, groupId, request);
	}

	@Test
	@DisplayName("문제 생성 실패 : solved.ac의 잘못된 response")
	void createProblemFailed_4() throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder()
			.link("link")
			.startDate(LocalDate.now())
			.endDate(LocalDate.now())
			.build();
		doThrow(new SolvedAcApiErrorException(HttpStatus.SERVICE_UNAVAILABLE.value(),
			"solved.ac API로부터 예상치 못한 응답을 받았습니다.")).when(
			problemService).createProblem(user, groupId, request);
		// when, then
		mockMvc.perform(post("/api/groups/{groupId}/problems", groupId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.error").value("solved.ac API로부터 예상치 못한 응답을 받았습니다."));
		verify(problemService, times(1)).createProblem(user, groupId, request);
	}

	@Test
	@DisplayName("문제 진행 기간 수정 성공")
	void editProblemDeadline() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(LocalDate.now(), LocalDate.now().plusDays(10));
		doNothing().when(problemService).editProblem(any(User.class), anyLong(), any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
		verify(problemService, times(1)).editProblem(user, problemId, request);
	}

	@Test
	@DisplayName("문제 진행 기간 수정 실패 : 존재하지 않는 문제")
	void editProblemDeadlineFailed_2() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(LocalDate.now(), LocalDate.now().plusDays(10));
		doThrow(new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다.")).when(problemService)
			.editProblem(any(User.class), anyLong(), any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("존재하지 않는 문제 입니다."));
		verify(problemService, times(1)).editProblem(user, problemId, request);
	}

	@Test
	@DisplayName("문제 진행 기간 수정 실패 : 권한 없음")
	void editProblemDeadlineFailed_3() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(LocalDate.now(), LocalDate.now().plusDays(10));
		doThrow(new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
			"문제 수정 권한이 없습니다. 방장, 부방장일 경우에만 수정이 가능합니다.")).when(problemService)
			.editProblem(any(User.class), anyLong(), any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제 수정 권한이 없습니다. 방장, 부방장일 경우에만 수정이 가능합니다."));
		verify(problemService, times(1)).editProblem(user, problemId, request);
	}

	@Test
	@DisplayName("문제 진행 기간 수정 실패 : 이미 진행 중인 문제인데 시작날짜 수정을 요청하는 경우")
	void editProblemDeadlineFailed_4() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(LocalDate.now(), LocalDate.now().plusDays(10));
		doThrow(
			new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제 시작 날짜 수정이 불가합니다. : 이미 진행 중인 문제입니다.")).when(
				problemService)
			.editProblem(any(User.class), anyLong(), any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제 시작 날짜 수정이 불가합니다. : 이미 진행 중인 문제입니다."));
		verify(problemService, times(1)).editProblem(user, problemId, request);
	}

	@Test
	@DisplayName("문제 진행 기간 수정 실패 : 문제 시작 날짜를 오늘 이전의 날짜로 요청한 경우")
	void editProblemDeadlineFailed_5() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(LocalDate.now().minusDays(10), LocalDate.now());
		doThrow(
			new ProblemValidationException(HttpStatus.BAD_REQUEST.value(), "문제 시작 날짜는 오늘 이전의 날짜로 수정할 수 없습니다.")).when(
				problemService)
			.editProblem(any(User.class), anyLong(), any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("문제 시작 날짜는 오늘 이전의 날짜로 수정할 수 없습니다."));
		verify(problemService, times(1)).editProblem(user, problemId, request);
	}

	@Test
	@DisplayName("문제 진행 기간 수정 실패 : 문제 마감 날짜를 오늘 이전의 날짜로 요청한 경우")
	void editProblemDeadlineFailed_6() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(LocalDate.now().minusDays(20),
			LocalDate.now().minusDays(10));
		doThrow(
			new ProblemValidationException(HttpStatus.BAD_REQUEST.value(), "문제 마감 날짜는 오늘 이전의 날짜로 수정할 수 없습니다.")).when(
				problemService)
			.editProblem(any(User.class), anyLong(), any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("문제 마감 날짜는 오늘 이전의 날짜로 수정할 수 없습니다."));
		verify(problemService, times(1)).editProblem(user, problemId, request);
	}

	@Test
	@DisplayName("진행 중인 문제 목록 조회 성공")
	void getProblemList() throws Exception {
		// given
		Pageable pageable = PageRequest.of(0, 20, Sort.by("endDate").descending());
		Page<GetProblemResponse> response = new PageImpl<>(new ArrayList<>());
		when(problemService.getInProgressProblems(any(User.class), anyLong(), any(Pageable.class))).thenReturn(
			response);
		// when, then
		mockMvc.perform(get("/api/groups/{groupId}/problems/in-progress", groupId)
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(content().string(objectMapper.writeValueAsString(response)));
		verify(problemService, times(1)).getInProgressProblems(user, groupId, pageable);
	}

	@Test
	@DisplayName("문제 목록 조회 실패 : 권한 없음")
	void getProblemListFailed_1() throws Exception {
		// given
		Pageable pageable = PageRequest.of(0, 20, Sort.by("endDate").descending());
		when(problemService.getInProgressProblems(any(User.class), anyLong(), any(Pageable.class))).thenThrow(
			new ProblemValidationException(HttpStatus.FORBIDDEN.value(), "문제를 조회할 권한이 없습니다."));
		// when, then
		mockMvc.perform(get("/api/groups/{groupId}/problems/in-progress", groupId)
				.header("Authorization", token))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제를 조회할 권한이 없습니다."));
		verify(problemService, times(1)).getInProgressProblems(user, groupId, pageable);
	}

	@Test
	@DisplayName("문제 삭제 성공")
	void deleteProblem() throws Exception {
		// given
		doNothing().when(problemService).deleteProblem(user, problemId);
		// when, then
		mockMvc.perform(delete("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isOk());
		verify(problemService, times(1)).deleteProblem(user, problemId);
	}

	@Test
	@DisplayName("문제 삭제 실패 : 존재하지 않는 문제")
	void deleteProblemFailed_1() throws Exception {
		// given
		doThrow(new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다.")).when(problemService)
			.deleteProblem(user, problemId);
		// when, then
		mockMvc.perform(delete("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("존재하지 않는 문제 입니다."));
		verify(problemService, times(1)).deleteProblem(user, problemId);
	}

	@Test
	@DisplayName("문제 삭제 실패 : 권한 없음")
	void deleteProblemFailed_2() throws Exception {
		// given
		doThrow(new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(),
			"문제 삭제 권한이 없습니다. 방장, 부방장일 경우에만 삭제가 가능합니다.")).when(problemService)
			.deleteProblem(user, problemId);
		// when, then
		mockMvc.perform(delete("/api/problems/{problemId}", problemId)
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제 삭제 권한이 없습니다. 방장, 부방장일 경우에만 삭제가 가능합니다."));
		verify(problemService, times(1)).deleteProblem(user, problemId);
	}
}