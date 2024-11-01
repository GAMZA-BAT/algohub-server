package com.gamzabat.algohub.feature.group.ranking.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.config.SpringSecurityConfig;
import com.gamzabat.algohub.feature.group.ranking.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.group.ranking.service.RankingService;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@WebMvcTest(RankingController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class RankingControllerTest {
	private final String token = "token";
	private final Long groupId = 1L;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private RankingService rankingService;
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
	@DisplayName("전체 랭킹 조회 성공")
	void getAllRank() throws Exception {
		// given
		List<GetRankingResponse> response = new ArrayList<>();
		when(rankingService.getAllRank(user, groupId)).thenReturn(response);
		// when, then
		mockMvc.perform(get("/api/group/all-ranking")
				.header("Authorization", token)
				.param("groupId", String.valueOf(groupId)))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(response)));

		verify(rankingService, times(1)).getAllRank(any(User.class), anyLong());
	}

	@Test
	@DisplayName("전체 랭킹 조회 실패 : 그룹을 못 찾은 경우")
	void getAllRankFailed_1() throws Exception {
		// given
		doThrow(new CannotFoundGroupException("그룹을 찾을 수 없습니다.")).when(rankingService).getAllRank(user, groupId);
		// when, then
		mockMvc.perform(get("/api/group/all-ranking")
				.header("Authorization", token)
				.param("groupId", String.valueOf(groupId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("그룹을 찾을 수 없습니다."));

		verify(rankingService, times(1)).getAllRank(any(User.class), anyLong());
	}

	@Test
	@DisplayName("전체 랭킹 조회 실패 : 랭킹 확인 권한이 없는 경우")
	void getAllRankFailed_2() throws Exception {
		// given
		doThrow(new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "랭킹을 확인할 권한이 없습니다.")).when(
			rankingService).getAllRank(user, groupId);
		// when, then
		mockMvc.perform(get("/api/group/all-ranking")
				.header("Authorization", token)
				.param("groupId", String.valueOf(groupId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("랭킹을 확인할 권한이 없습니다."));

		verify(rankingService, times(1)).getAllRank(any(User.class), anyLong());
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 성공")
	void getTopRank() throws Exception {
		// given
		List<GetRankingResponse> response = new ArrayList<>();
		when(rankingService.getAllRank(user, groupId)).thenReturn(response);
		// when, then
		mockMvc.perform(get("/api/group/top-ranking")
				.header("Authorization", token)
				.param("groupId", String.valueOf(groupId)))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(response)));

		verify(rankingService, times(1)).getTopRank(any(User.class), anyLong());
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 실패 : 그룹을 못 찾은 경우")
	void getTopRankFailed_1() throws Exception {
		// given
		doThrow(new CannotFoundGroupException("그룹을 찾을 수 없습니다.")).when(rankingService).getTopRank(user, groupId);
		// when, then
		mockMvc.perform(get("/api/group/top-ranking")
				.header("Authorization", token)
				.param("groupId", String.valueOf(groupId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("그룹을 찾을 수 없습니다."));

		verify(rankingService, times(1)).getTopRank(any(User.class), anyLong());
	}

	@Test
	@DisplayName("Top 3 랭킹 조회 실패 : 랭킹 확인 권한이 없는 경우")
	void getTopRankFailed_2() throws Exception {
		// given
		doThrow(new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "랭킹을 확인할 권한이 없습니다.")).when(
			rankingService).getTopRank(user, groupId);
		// when, then
		mockMvc.perform(get("/api/group/top-ranking")
				.header("Authorization", token)
				.param("groupId", String.valueOf(groupId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error").value("랭킹을 확인할 권한이 없습니다."));

		verify(rankingService, times(1)).getTopRank(any(User.class), anyLong());
	}
}
