package com.gamzabat.algohub.feature.joinRequest;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

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
import com.gamzabat.algohub.enums.JoinRequestStatus;
import com.gamzabat.algohub.feature.group.studygroup.controller.JoinRequestController;
import com.gamzabat.algohub.feature.group.studygroup.domain.JoinRequest;
import com.gamzabat.algohub.feature.group.studygroup.dto.UpdateJoinRequestStatusRequest;
import com.gamzabat.algohub.feature.group.studygroup.exception.JoinRequestException;
import com.gamzabat.algohub.feature.group.studygroup.service.JoinRequestService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

@WebMvcTest(JoinRequestController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class JoinRequestControllerTest {
	private final String token = "token";
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JoinRequestService joinRequestService;
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
	@DisplayName("가입 요청 성공")
	void joinRequestSuccess() throws Exception {
		//given
		Long groupId = 10L;
		willDoNothing().given(joinRequestService).joinRequest(any(User.class), eq(groupId));

		mockMvc.perform(post("/api/groups/{groupId}/join-request", groupId)
				.header("Authorization", token))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("가입 요청 실패 : 이미 요청한 그룹")
	void joinRequest_fail_alreadyRequested() throws Exception {
		Long groupId = 10L;
		willThrow(new JoinRequestException("이미 요청한 그룹입니다."))
			.given(joinRequestService).joinRequest(any(User.class), eq(groupId));

		mockMvc.perform(post("/api/groups/{groupId}/join-request", groupId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("가입 요청 목록 조회")
	void getAllJoinRequests_success() throws Exception {
		Long groupId = 10L;
		// 직렬화가 비어도 배열 길이만 확인할 수 있게 더미 객체 2개
		given(joinRequestService.getAllJoinRequests(any(User.class), eq(groupId)))
			.willReturn(List.of(new JoinRequest(), new JoinRequest()));

		mockMvc.perform(get("/api/groups/{groupId}/join-request", groupId)
				.header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	@DisplayName("요청 목록 조회 실패 : 권한 없음")
	void getAllJoinRequests_fail_alreadyRequested() throws Exception {
		Long groupId = 10L;
		given(joinRequestService.getAllJoinRequests(any(User.class), eq(groupId)))
			.willThrow(new JoinRequestException("요청 목록을 조회할 권한이 없습니다."));

		mockMvc.perform(get("/api/groups/{groupId}/join-request", groupId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("승인 성공")
	void approve_success() throws Exception {
		Long requestId = 77L;
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.APPROVE);
		willDoNothing().given(joinRequestService)
			.updateJoinRequest(any(User.class), eq(requestId), eq(request));

		mockMvc.perform(post("/api/join-request/{requestId}", requestId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("승인 실패 해당 요청 없음")
	void approve_fail_requestNotFound() throws Exception {
		Long groupId = 10L;
		Long requestId = 999L;
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.APPROVE);
		willThrow(new JoinRequestException("해당 요청이 존재하지 않습니다"))
			.given(joinRequestService).updateJoinRequest(any(User.class), eq(requestId), eq(request));

		mockMvc.perform(post("/api/join-request/{requestId}", requestId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("가입 요청 거절 성공")
	void reject_success() throws Exception {
		Long requestId = 77L;
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.REJECT);

		willDoNothing().given(joinRequestService)
			.updateJoinRequest(any(User.class), eq(requestId), eq(request));

		mockMvc.perform(post("/api/join-request/{requestId}", requestId)
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("가입 요청 거절 실패 권한 없음")
	void reject_fail_noPermission() throws Exception {
		Long requestId = 77L;
		UpdateJoinRequestStatusRequest request = new UpdateJoinRequestStatusRequest(JoinRequestStatus.REJECT);

		willThrow(new JoinRequestException("승인 권한이 없습니다."))
			.given(joinRequestService).updateJoinRequest(any(User.class), eq(requestId), eq(request));

		mockMvc.perform(post("/api/join-request/{requestId}", requestId)
				.header("Authorization", token))
			.andExpect(status().isBadRequest());
	}
}
