package com.gamzabat.algohub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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
import com.gamzabat.algohub.feature.edgecase.dto.GetEdgeCaseListResponse;
import com.gamzabat.algohub.feature.edgecase.dto.GetEdgeCaseResponse;
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
	private EdgeCase edgeCase1, edgeCase2, edgeCase3;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image").build();


		edgeCase1 = EdgeCase.builder()
			.level(3)
			.link("https://www.acmicpc.net/problem/1001")
			.problemNumber(1001)
			.title("A-B")
			.input("0 0")
			.output("0")
			.like(5)
			.author(user)
			.build();

		edgeCase2 = EdgeCase.builder()
			.level(3)
			.link("https://www.acmicpc.net/problem/1001")
			.problemNumber(1001)
			.title("A-B")
			.input("5 5")
			.output("0")
			.like(12)
			.author(user)
			.build();

		edgeCase3 = EdgeCase.builder()
			.level(3)
			.link("https://www.acmicpc.net/problem/1002")
			.problemNumber(1002)
			.title("Turret")
			.input("0 0 13 40 0 37")
			.output("2")
			.like(21)
			.author(user)
			.build();

		Field userId = User.class.getDeclaredField("id");
		userId.setAccessible(true);
		userId.set(user, 1L);

		Field edgeCaseId = EdgeCase.class.getDeclaredField("id");
		edgeCaseId.setAccessible(true);
		edgeCaseId.set(edgeCase1, 1L);
		edgeCaseId.set(edgeCase2, 2L);
		edgeCaseId.set(edgeCase3, 3L);
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
		assertThat(result.getProblemNumber()).isEqualTo(1000);
		assertThat(result.getTitle()).isEqualTo("A+B");
		assertThat(result.getLevel()).isEqualTo(1);
		assertThat(result.getInput()).isEqualTo("1 2");
		assertThat(result.getOutput()).isEqualTo("3");
		assertThat(result.getLike()).isEqualTo(0);
		assertThat(result.getAuthor()).isEqualTo(user);

	}

	@Test
	@DisplayName("반례 리스트 조회 성공 // 여러개 반환")
	void getEdgeCaseListSuccess_1() {
		//given
		Integer problemNumber = 1001;
		List<EdgeCase> edgeCaseList = Arrays.asList(edgeCase1, edgeCase2);
		when(edgeCaseRepository.findAllByProblemNumber(problemNumber))
			.thenReturn(edgeCaseList);

		//when
		GetEdgeCaseListResponse response = edgeCaseService.getEdgeCaseList(problemNumber);

		//then
		assertEquals(2, response.getEdgeCaseList().size());

		GetEdgeCaseResponse firstResponse = response.getEdgeCaseList().get(0);
		assertEquals(3, firstResponse.getLevel());
		assertEquals(1001, firstResponse.getProblemNumber());
		assertEquals("A-B", firstResponse.getTitle());
		assertEquals("0 0", firstResponse.getInput());
		assertEquals("0", firstResponse.getOutput());
		assertEquals(5, firstResponse.getLike());

		GetEdgeCaseResponse secondResponse = response.getEdgeCaseList().get(1);
		assertEquals(3, secondResponse.getLevel());
		assertEquals(1001, secondResponse.getProblemNumber());
		assertEquals("A-B", secondResponse.getTitle());
		assertEquals("5 5", secondResponse.getInput());
		assertEquals("0", secondResponse.getOutput());
		assertEquals(12, secondResponse.getLike());
	}

	@Test
	@DisplayName("반례 리스트 조회 성공 // 모든 리스트 반환")
	void getEdgeCaseListSuccess_2() {
		// given
		Integer problemNumber = null;
		List<EdgeCase> edgeCaseList = Arrays.asList(edgeCase1, edgeCase2, edgeCase3);
		when(edgeCaseRepository.findAll())
			.thenReturn(edgeCaseList);
		// when
		GetEdgeCaseListResponse response = edgeCaseService.getEdgeCaseList(problemNumber);

		// then
		assertEquals(3, response.getEdgeCaseList().size());

		GetEdgeCaseResponse firstResponse = response.getEdgeCaseList().get(0);
		assertEquals(3, firstResponse.getLevel());
		assertEquals(1001, firstResponse.getProblemNumber());
		assertEquals("A-B", firstResponse.getTitle());
		assertEquals("0 0", firstResponse.getInput());
		assertEquals("0", firstResponse.getOutput());
		assertEquals(5, firstResponse.getLike());

		GetEdgeCaseResponse secondResponse = response.getEdgeCaseList().get(1);
		assertEquals(3, secondResponse.getLevel());
		assertEquals(1001, secondResponse.getProblemNumber());
		assertEquals("A-B", secondResponse.getTitle());
		assertEquals("5 5", secondResponse.getInput());
		assertEquals("0", secondResponse.getOutput());
		assertEquals(12, secondResponse.getLike());

		GetEdgeCaseResponse thirdResponse = response.getEdgeCaseList().get(2);
		assertEquals(3, thirdResponse.getLevel());
		assertEquals(1002, thirdResponse.getProblemNumber());
		assertEquals("Turret", thirdResponse.getTitle());
		assertEquals("0 0 13 40 0 37", thirdResponse.getInput());
		assertEquals("2", thirdResponse.getOutput());
		assertEquals(21, thirdResponse.getLike());
	}
}

