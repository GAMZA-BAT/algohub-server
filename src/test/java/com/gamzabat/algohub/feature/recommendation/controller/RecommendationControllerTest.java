package com.gamzabat.algohub.feature.recommendation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.config.SpringSecurityConfig;
import com.gamzabat.algohub.feature.recommendation.domain.TagType;
import com.gamzabat.algohub.feature.recommendation.dto.HomeRecommendationsResponse;
import com.gamzabat.algohub.feature.recommendation.dto.RecommendationItemDto;
import com.gamzabat.algohub.feature.recommendation.dto.StudyGroupSummaryDto;
import com.gamzabat.algohub.feature.recommendation.service.RecommendationService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@WebMvcTest(controllers = RecommendationController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class RecommendationControllerTest {

	private final String token = "token";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RecommendationService recommendationService;
	@MockBean
	private TokenProvider tokenProvider;
	@MockBean
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		user = org.mockito.Mockito.mock(User.class);
		when(user.getId()).thenReturn(42L);
		when(user.getEmail()).thenReturn("email");

		when(tokenProvider.getUserEmail(token)).thenReturn("email");
		when(userRepository.findByEmail("email")).thenReturn(java.util.Optional.of(user));
	}

	@Test
	@DisplayName("GET /api/home/recommendations 정상 응답")
	void getHomeRecommendations_ok() throws Exception {
		StudyGroupSummaryDto g1 = new StudyGroupSummaryDto(1L, "A", "intro", "img",
			java.util.List.of(TagType.MOST_ACTIVE_THIS_WEEK.name()));
		StudyGroupSummaryDto g2 = new StudyGroupSummaryDto(2L, "B", "intro", "img",
			java.util.List.of(TagType.HIGH_JOIN_RATE_RECENT.name()));
		StudyGroupSummaryDto g3 = new StudyGroupSummaryDto(3L, "C", "intro", "img",
			java.util.List.of(TagType.SIMILAR_DIFFICULTY.name()));

		HomeRecommendationsResponse response = new HomeRecommendationsResponse(
			new RecommendationItemDto(TagType.MOST_ACTIVE_THIS_WEEK, 10.0, g1),
			new RecommendationItemDto(TagType.HIGH_JOIN_RATE_RECENT, 0.3, g2),
			new RecommendationItemDto(TagType.SIMILAR_DIFFICULTY, 0.1, g3)
		);

		when(recommendationService.getHomeRecommendations(any())).thenReturn(response);

		mockMvc.perform(get("/api/home/recommendations")
				.header("Authorization", token)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(response)));

		verify(recommendationService, times(1)).getHomeRecommendations(any());
	}
}
