package com.gamzabat.algohub.feature.problem.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.config.SpringSecurityConfig;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.problem.dto.CreateProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.EditProblemRequest;
import com.gamzabat.algohub.feature.problem.dto.GetProblemResponse;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@WebMvcTest(ProblemController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class ProblemControllerTest {
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
	private final String token = "token";
	private final Long groupId = 1L;
	private final Long problemId = 10L;
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
		CreateProblemRequest request = CreateProblemRequest.builder().groupId(groupId).link("link").deadline(LocalDate.now()).build();
		doNothing().when(problemService).createProblem(any(User.class),any(CreateProblemRequest.class));
		// when, then
		mockMvc.perform(post("/api/problem")
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(content().string("OK"));
		verify(problemService,times(1)).createProblem(user,request);
	}

	@ParameterizedTest
	@CsvSource(value = {
		"null,link,'2024-07-18',groupId : 그룹 고유 아이디는 필수 입력 입니다. ",
		"1,'','2024-07-18',link : 문제 링크는 필수 입력 입니다.",
		"1,link, null, deadline : 마감 기한은 필수 입력 입니다."
	},nullValues = "null")
	@DisplayName("문제 생성 실패 : 잘못된 요청")
	void createProblemFailed_1(Long groupId, String link, LocalDate deadline, String exceptionMessage) throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder().groupId(groupId).link(link).deadline(deadline).build();
		// when, then
		mockMvc.perform(post("/api/problem")
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("문제 생성 요청이 올바르지 않습니다."))
			.andExpect(jsonPath("$.messages",hasItems(exceptionMessage)));
	}

	@Test
	@DisplayName("문제 생성 실패 : 권한 없음")
	void createProblemFailed_2() throws Exception {
		// given
		CreateProblemRequest request = CreateProblemRequest.builder().groupId(groupId).link("link").deadline(LocalDate.now()).build();
		doThrow(new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "문제에 대한 권한이 없습니다. : create")).when(problemService).createProblem(user,request);
		// when, then
		mockMvc.perform(post("/api/problem")
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제에 대한 권한이 없습니다. : create"));
		verify(problemService,times(1)).createProblem(user,request);
	}

	@Test
	@DisplayName("문제 마감기한 수정 성공")
	void editProblemDeadline() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(problemId, LocalDate.now().plusDays(10));
		doNothing().when(problemService).editProblem(any(User.class),any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problem")
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(content().string("OK"));
		verify(problemService, times(1)).editProblem(user,request);
	}

	@ParameterizedTest
	@CsvSource(value = {
		"null,'2024-08-21',problemId : 문제 고유 아이디는 필수 입력 입니다.",
		"10, null, deadline : 마감 기한은 필수 입력 입니다."
	},nullValues = "null")
	@DisplayName("문제 마감기한 수정 실패 : 잘못된 요청")
	void editProblemDeadlineFailed_1(Long problemId, LocalDate deadline, String exceptionMessage) throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(problemId,deadline);
		// when, then
		mockMvc.perform(patch("/api/problem")
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("문제 마감 기한 수정 요청이 올바르지 않습니다."))
			.andExpect(jsonPath("$.messages",hasItem(exceptionMessage)));
	}

	@Test
	@DisplayName("문제 마감기한 수정 실패 : 존재하지 않는 문제")
	void editProblemDeadlineFailed_2() throws Exception {
		// given
		EditProblemRequest request = new EditProblemRequest(problemId, LocalDate.now().plusDays(10));
		doThrow(new ProblemValidationException(HttpStatus.NOT_FOUND.value(),"존재하지 않는 문제 입니다.")).when(problemService).editProblem(any(User.class),any(EditProblemRequest.class));
		// when, then
		mockMvc.perform(patch("/api/problem")
				.header("Authorization",token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("존재하지 않는 문제 입니다."));
		verify(problemService, times(1)).editProblem(user,request);
	}

	@Test
	@DisplayName("문제 목록 조회 성공")
	void getProblemList() throws Exception {
		// given
		List<GetProblemResponse> response = new ArrayList<>(30);
		when(problemService.getProblemList(any(User.class),anyLong())).thenReturn(response);
		// when, then
		mockMvc.perform(get("/api/problem")
				.header("Authorization",token)
				.param("groupId",String.valueOf(groupId)))
			.andExpect(status().isOk())
			.andExpect(content().string(objectMapper.writeValueAsString(response)));
		verify(problemService,times(1)).getProblemList(user,groupId);
	}

	@Test
	@DisplayName("문제 목록 조회 실패 : 권한 없음")
	void getProblemListFailed_1() throws Exception {
		// given
		when(problemService.getProblemList(any(User.class),anyLong())).thenThrow(new ProblemValidationException(HttpStatus.FORBIDDEN.value(),"문제를 조회할 권한이 없습니다."));
		// when, then
		mockMvc.perform(get("/api/problem")
				.header("Authorization",token)
				.param("groupId",String.valueOf(groupId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("문제를 조회할 권한이 없습니다."));
		verify(problemService,times(1)).getProblemList(user,groupId);
	}

	@Test
	@DisplayName("문제 삭제 성공")
	void deleteProblem() throws Exception {
		// given
		doNothing().when(problemService).deleteProblem(user,problemId);
		// when, then
		mockMvc.perform(delete("/api/problem")
			.header("Authorization",token)
				.param("problemId",String.valueOf(problemId)))
			.andExpect(status().isOk())
			.andExpect(content().string("OK"));
		verify(problemService,times(1)).deleteProblem(user,problemId);
	}

	@Test
	@DisplayName("문제 삭제 실패 : 존재하지 않는 문제")
	void deleteProblemFailed_1() throws Exception {
		// given
		doThrow(new ProblemValidationException(HttpStatus.NOT_FOUND.value(),"존재하지 않는 문제 입니다.")).when(problemService).deleteProblem(user,problemId);
		// when, then
		mockMvc.perform(delete("/api/problem")
				.header("Authorization",token)
				.param("problemId",String.valueOf(problemId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("존재하지 않는 문제 입니다."));
		verify(problemService,times(1)).deleteProblem(user,problemId);
	}
}