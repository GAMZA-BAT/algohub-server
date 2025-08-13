package com.gamzabat.algohub.service;

import static com.mysema.commons.lang.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;
import com.gamzabat.algohub.feature.edgecase.dto.CreateEdgeCaseRequest;
import com.gamzabat.algohub.feature.edgecase.repository.EdgeCaseRepository;
import com.gamzabat.algohub.feature.edgecase.service.EdgeCaseService;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class EdgeCaseServiceTest {
	@InjectMocks
	private EdgeCaseService edgeCaseService;

	@Captor
	private ArgumentCaptor<EdgeCase> edgeCaseCaptor;

	@Mock
	private EdgeCaseRepository edgeCaseRepository;

	@Mock
	private ProblemService problemService;

	private User user;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image").build();
	}

	@Test
	@DisplayName("반례 생성 성공")
	void createEdgeCaseSuccess() {
		//given
		CreateEdgeCaseRequest request = CreateEdgeCaseRequest.builder()
			.link("https://www.acmicpc.net/problem/1000")
			.input("1 2")
			.output("3")
			.build();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode mockJsonNode = mapper.createObjectNode()
			.put("title", "A+B")
			.put("problemId", 1234)
			.put("level", 1);
		given(problemService.fetchProblemDetails(anyString())).willReturn(mockJsonNode);
		given(problemService.getProblemLevel(mockJsonNode)).willReturn(1);
		given(problemService.getProblemTitle(mockJsonNode)).willReturn("A+B");
		//when
		edgeCaseService.createEdgeCase(user, request);

		//then
		verify(edgeCaseRepository, times(1)).save(edgeCaseCaptor.capture());

		EdgeCase result = edgeCaseCaptor.getValue();
		assertThat(result.getLink()).isEqualTo("https://www.acmicpc.net/problem/1000");
		assertThat(result.getNumber()).isEqualTo(1000);
		assertThat(result.getTitle()).isEqualTo("A+B");
		assertThat(result.getLevel()).isEqualTo(1);
		assertThat(result.getInput()).isEqualTo("1 2");
		assertThat(result.getOutput()).isEqualTo("3");
		assertThat(result.getLike()).isEqualTo(0);
		assertThat(result.getAuthor()).isEqualTo(user);

	}
}
