package com.gamzabat.algohub.feature.solution.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.config.SpringSecurityConfig;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.comment.repository.CommentRepository;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.dto.GetSolutionResponse;
import com.gamzabat.algohub.feature.solution.exception.CannotFoundSolutionException;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.solution.service.SolutionService;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@WebMvcTest(SolutionController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class SolutionControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private SolutionRepository solutionRepository;
	@MockBean
	private ProblemRepository problemRepository;
	@MockBean
	private StudyGroupRepository studyGroupRepository;
	@MockBean
	private GroupMemberRepository groupMemberRepository;
	@MockBean
	private UserRepository userRepository;
	@MockBean
	private CommentRepository commentRepository;
	@MockBean
	private TokenProvider tokenProvider;
	@MockBean
	private SolutionService solutionService;

	private User user;
	private final String token = "token";
	private final Long groupId = 1L;
	private final Long problemId = 10L;
	private final Long solutionId = 100L;

	@BeforeEach
	void setUp() {
		user = User.builder().email("email").password("password").build();
		when(tokenProvider.getUserEmail(token)).thenReturn("email");
		when(userRepository.findByEmail("email")).thenReturn(Optional.ofNullable(user));
	}

	@Test
	@DisplayName("풀이 목록 조회 성공")
	void getSolutionList_1() throws Exception {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		GetSolutionResponse response = GetSolutionResponse.builder().build();
		Page<GetSolutionResponse> pagedResponse = new PageImpl<>(Collections.singletonList(response), pageable, 1);
		when(solutionService.getSolutionList(any(User.class), anyLong(), isNull(), isNull(), isNull(),
			any(Pageable.class))).thenReturn(pagedResponse);
		// when, then
		mockMvc.perform(get("/api/solution")
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isOk())
			.andExpect(content().string(objectMapper.writeValueAsString(pagedResponse)));
		verify(solutionService, times(1)).getSolutionList(user, problemId, null, null, null, pageable);
	}

	@Test
	@DisplayName("풀이 목록 조회 실패 : 존재하지 않는 문제")
	void getSolutionListFailed_1() throws Exception {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		when(solutionService.getSolutionList(any(User.class), anyLong(), isNull(), isNull(), isNull(),
			any(Pageable.class)))
			.thenThrow(new ProblemValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 문제 입니다."));
		// when, then
		mockMvc.perform(get("/api/solution")
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("존재하지 않는 문제 입니다."));
		verify(solutionService, times(1)).getSolutionList(user, problemId, null, null, null, pageable);
	}

	@Test
	@DisplayName("풀이 목록 조회 실패 : 존재하지 않는 그룹")
	void getSolutionListFailed_2() throws Exception {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		when(solutionService.getSolutionList(any(User.class), anyLong(), isNull(), isNull(), isNull(),
			any(Pageable.class)))
			.thenThrow(new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
		// when, then
		mockMvc.perform(get("/api/solution")
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("존재하지 않는 그룹 입니다."));
		verify(solutionService, times(1)).getSolutionList(user, problemId, null, null, null, pageable);
	}

	@Test
	@DisplayName("풀이 목록 조회 실패 : 참여하지 않은 그룹")
	void getSolutionListFailed_3() throws Exception {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		when(solutionService.getSolutionList(any(User.class), anyLong(), isNull(), isNull(), isNull(),
			any(Pageable.class)))
			.thenThrow(new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));
		// when, then
		mockMvc.perform(get("/api/solution")
				.header("Authorization", token)
				.param("problemId", String.valueOf(problemId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("참여하지 않은 그룹 입니다."));
		verify(solutionService, times(1)).getSolutionList(user, problemId, null, null, null, pageable);
	}

	@Test
	@DisplayName("풀이 하나 조회 성공")
	void getSolution_1() throws Exception {
		// given
		GetSolutionResponse response = GetSolutionResponse.builder().build();
		when(solutionService.getSolution(any(User.class), anyLong())).thenReturn(response);
		// when, then
		mockMvc.perform(get("/api/solution/{solutionId}", solutionId)
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(content().string(objectMapper.writeValueAsString(response)));
		verify(solutionService, times(1)).getSolution(user, solutionId);
	}

	@Test
	@DisplayName("풀이 하나 조회 실패 : 존재하지 않는 풀이")
	void getSolutionFailed_1() throws Exception {
		// given
		when(solutionService.getSolution(any(User.class), eq(solutionId)))
			.thenThrow(new CannotFoundSolutionException("존재하지 않는 풀이 입니다."));
		// when, then
		mockMvc.perform(get("/api/solution/{solutionId}", solutionId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("존재하지 않는 풀이 입니다."));
		verify(solutionService, times(1)).getSolution(user, solutionId);
	}

	@Test
	@DisplayName("풀이 하나 조회 실패 : 존재하지 않는 풀이")
	void getSolutionFailed_2() throws Exception {
		// given
		when(solutionService.getSolution(any(User.class), eq(solutionId)))
			.thenThrow(new CannotFoundSolutionException("존재하지 않는 풀이 입니다."));
		// when, then
		mockMvc.perform(get("/api/solution/{solutionId}", solutionId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("존재하지 않는 풀이 입니다."));
		verify(solutionService, times(1)).getSolution(user, solutionId);
	}

	@Test
	@DisplayName("풀이 하나 조회 실패 : 풀이 조회 권한 없음")
	void getSolutionFailed_3() throws Exception {
		// given
		when(solutionService.getSolution(any(User.class), eq(solutionId)))
			.thenThrow(new UserValidationException("해당 풀이를 확인 할 권한이 없습니다."));
		// when, then
		mockMvc.perform(get("/api/solution/{solutionId}", solutionId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("해당 풀이를 확인 할 권한이 없습니다."));
		verify(solutionService, times(1)).getSolution(user, solutionId);
	}

}