package com.gamzabat.algohub.feature.studygroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamzabat.algohub.common.jwt.TokenProvider;
import com.gamzabat.algohub.config.SpringSecurityConfig;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.exception.CannotFoundSolutionException;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.dto.*;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.studygroup.service.StudyGroupService;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyGroupController.class)
@WithMockUser
@Import(SpringSecurityConfig.class)
class StudyGroupControllerTest {
    private final Long userId = 0L;
    private final String token = "token";
    private final Long groupId = 1L;
    private final String code = "invitationCode";
    private final Long problemId = 10L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private StudyGroupService studyGroupService;
    @MockBean
    private StudyGroupRepository studyGroupRepository;
    @MockBean
    private GroupMemberRepository groupMemberRepository;
    @MockBean
    private ImageService imageService;
    @MockBean
    private SolutionRepository solutionRepository;
    @MockBean
    private ProblemRepository problemRepository;
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
    @DisplayName("그룹 생성 성공")
    void createGroup() throws Exception {
        // given
        CreateGroupRequest request = new CreateGroupRequest("name", LocalDate.now(), LocalDate.now().plusDays(30),
                "introduction");
        CreateGroupResponse response = new CreateGroupResponse("inviteCode");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsString(request).getBytes());
        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg",
                "image".getBytes());
        when(studyGroupService.createGroup(any(User.class), any(CreateGroupRequest.class),
                any(MultipartFile.class))).thenReturn(response);
        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(profileImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", token)
                        .with(request1 -> {
                            request1.setMethod("POST");
                            return request1;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(studyGroupService, times(1)).createGroup(user, request, profileImage);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'', '2024-07-01', '2024-07-29','', name : 스터디 이름은 1글자 이상 15글자 이하로 작성해야 합니다.",
            "name, null, '2024-07-29','', startDate : 스터디 시작 날짜는 필수 입력 입니다.",
            "name, '2024-07-01',null,'', endDate : 스터디 종료 날짜는 필수 입력 입니다."
    }, nullValues = "null")
    @DisplayName("그룹 생성 실패 : 잘못된 요청")
    void createGroupFailed_1(String name, LocalDate startDate, LocalDate endDate, String introduction,
                             String exceptionMessage) throws Exception {
        // given
        CreateGroupRequest request = new CreateGroupRequest(name, startDate, endDate, introduction);
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsString(request).getBytes());
        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg",
                "image".getBytes());
        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(profileImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("Authorization", token)
                        .with(request1 -> {
                            request1.setMethod("POST");
                            return request1;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("그룹 생성 요청이 올바르지 않습니다."))
                .andExpect(jsonPath("$.messages", hasItems(exceptionMessage)));
    }

    @Test
    @DisplayName("그룹 코드를 사용해 그룹 참여 성공")
    void joinGroupWithCode() throws Exception {
        // given
        doNothing().when(studyGroupService).joinGroupWithCode(any(User.class), anyString());
        // when, then
        mockMvc.perform(post("/api/group/{code}/join", code)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(studyGroupService, times(1)).joinGroupWithCode(user, code);
    }

    @Test
    @DisplayName("그룹 코드를 사용해 그룹 참여 실패 : 존재하지 않는 그룹")
    void joinGroupWithCodeFailed_1() throws Exception {
        // given
        doThrow(new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다.")).when(
                studyGroupService).joinGroupWithCode(any(User.class), anyString());
        // when, then
        mockMvc.perform(post("/api/group/{code}/join", code)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("존재하지 않는 그룹 입니다."));

        verify(studyGroupService, times(1)).joinGroupWithCode(user, code);
    }

    @Test
    @DisplayName("그룹 코드를 사용해 그룹 참여 실패 : 이미 참여한 그룹")
    void joinGroupWithCodeFailed_2() throws Exception {
        // given
        doThrow(new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "이미 참여한 그룹 입니다.")).when(
                studyGroupService).joinGroupWithCode(any(User.class), anyString());
        // when, then
        mockMvc.perform(post("/api/group/{code}/join", code)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 참여한 그룹 입니다."));

        verify(studyGroupService, times(1)).joinGroupWithCode(user, code);
    }

    @Test
    @DisplayName("그룹 목록 조회 성공")
    void getStudyGroupList() throws Exception {
        // given
        List<GetStudyGroupResponse> bookmarked = new ArrayList<>();
        List<GetStudyGroupResponse> done = new ArrayList<>();
        List<GetStudyGroupResponse> inProgress = new ArrayList<>();
        List<GetStudyGroupResponse> queued = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            bookmarked.add(new GetStudyGroupResponse(
                    (long) i, "name" + i, "groupImage" + 1,
                    LocalDate.now(), LocalDate.now().plusDays(i),
                    "introduction" + 1, "nickname", true, true
            ));
        }

        for (int i = 0; i < 10; i++) {
            done.add(new GetStudyGroupResponse(
                    (long) i, "name" + i, "groupImage" + 1,
                    LocalDate.now(), LocalDate.now().plusDays(i),
                    "introduction" + 1, "nickname", true, true
            ));
        }
        for (int i = 0; i < 10; i++) {
            inProgress.add(new GetStudyGroupResponse(
                    (long) i, "name" + i, "groupImage" + 1,
                    LocalDate.now(), LocalDate.now().plusDays(i),
                    "introduction" + 1, "nickname", true, true
            ));
        }
        for (int i = 0; i < 10; i++) {
            queued.add(new GetStudyGroupResponse(
                    (long) i, "name" + i, "groupImage" + 1,
                    LocalDate.now(), LocalDate.now().plusDays(i),
                    "introduction" + 1, "nickname", true, true
            ));
        }
        GetStudyGroupListsResponse response = new GetStudyGroupListsResponse(bookmarked, done, inProgress, queued);
        when(studyGroupService.getStudyGroupList(user)).thenReturn(response);
        // when, then
        mockMvc.perform(get("/api/group/list")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(studyGroupService, times(1)).getStudyGroupList(any(User.class));
    }

    @Test
    @DisplayName("그룹 탈퇴 성공")
    void leaveGroup() throws Exception {
        // given
        doNothing().when(studyGroupService).deleteGroup(user, groupId);
        // when, then
        mockMvc.perform(delete("/api/group/leave")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(studyGroupService, times(1)).deleteGroup(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 : 존재하지 않는 그룹")
    void leaveGroupFailed_1() throws Exception {
        // given
        doThrow(new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다.")).when(
                studyGroupService).deleteGroup(user, groupId);
        // when, then
        mockMvc.perform(delete("/api/group/leave")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("존재하지 않는 그룹 입니다."));

        verify(studyGroupService, times(1)).deleteGroup(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 : 이미 참여 안한 그룹")
    void leaveGroupFailed_2() throws Exception {
        // given
        doThrow(new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "이미 참여하지 않은 그룹 입니다.")).when(
                studyGroupService).deleteGroup(user, groupId);
        // when, then
        mockMvc.perform(delete("/api/group/leave")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미 참여하지 않은 그룹 입니다."));

        verify(studyGroupService, times(1)).deleteGroup(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 멤버 삭제 성공")
    void deleteUser() throws Exception {
        // given
        doNothing().when(studyGroupService).deleteMember(any(User.class), anyLong(), anyLong());
        // when, then
        mockMvc.perform(delete("/api/group/delete")
                        .header("Authorization", token)
                        .param("userId", String.valueOf(userId))
                        .param("groupId", String.valueOf(groupId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
        verify(studyGroupService, times(1)).deleteMember(user, userId, groupId);
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 : 참여하지 않은 그룹")
    void deleteMemberFailed_1() throws Exception {
        // given
        doThrow(new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(),
                "멤버 삭제 권한이 없습니다. : 참여하지 않은 그룹 입니다.")).when(
                studyGroupService).deleteGroup(user, groupId);
        // when, then
        mockMvc.perform(delete("/api/group/leave")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("멤버 삭제 권한이 없습니다. : 참여하지 않은 그룹 입니다."));
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 : 권한 없음")
    void deleteMemberFailed_2() throws Exception {
        // given
        doThrow(new UserValidationException("멤버를 삭제 할 권한이 없습니다.")).when(
                studyGroupService).deleteGroup(user, groupId);
        // when, then
        mockMvc.perform(delete("/api/group/leave")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("멤버를 삭제 할 권한이 없습니다."));
    }

    @Test
    @DisplayName("그룹 정보 수정 성공")
    void editGroup() throws Exception {
        // given
        EditGroupRequest request = new EditGroupRequest(groupId, "name", LocalDate.now(), LocalDate.now().plusDays(30),
                "editedIntroduction");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile groupImage = new MockMultipartFile("groupImage", "group.jpg", "image/jpeg",
                "image".getBytes());
        doNothing().when(studyGroupService)
                .editGroup(any(User.class), any(EditGroupRequest.class), any(MultipartFile.class));
        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(groupImage) // 파라미터 이름이랑 똑같이 해줘야 함
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(studyGroupService, times(1)).editGroup(any(User.class), any(EditGroupRequest.class),
                any(MultipartFile.class));
    }

    @Test
    @DisplayName("그룹 정보 수정 실패 : 잘못된 요청__잘못된 id")
    void editGroupFailed_1() throws Exception {
        // given
        EditGroupRequest request = new EditGroupRequest(null, "name", LocalDate.now(), LocalDate.now().plusDays(30),
                "editedIntroduction");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile groupImage = new MockMultipartFile("groupImage", "group.jpg", "image/jpeg",
                "image".getBytes());
        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(groupImage)
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("그룹 정보 수정 요청이 올바르지 않습니다."))
                .andExpect(jsonPath("$.messages", hasItems("id : 그룹 고유 아이디는 필수 입력 입니다.")));
    }

    @Test
    @DisplayName("그룹 정보 수정 실패 : 잘못된 요청__스터디 이름이 15글자 초과")
    void editGroupFailed_nameTooLong() throws Exception {
        // given
        EditGroupRequest request = new EditGroupRequest(1L, "이름이너무긴스터디이름이네요하하", LocalDate.now(), LocalDate.now().plusDays(30),
                "editedIntroduction"); // 16글자
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile groupImage = new MockMultipartFile("groupImage", "group.jpg", "image/jpeg",
                "image".getBytes());

        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(groupImage)
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("그룹 정보 수정 요청이 올바르지 않습니다."))
                .andExpect(jsonPath("$.messages", hasItems("name : 스터디 이름은 1글자 이상 15글자 이하로 작성해야 합니다.")));
    }

    @Test
    @DisplayName("그룹 정보 수정 실패 : 존재하지 않는 그룹")
    void editGroupFailed_2() throws Exception {
        // given
        EditGroupRequest request = new EditGroupRequest(groupId, "name", LocalDate.now(), LocalDate.now().plusDays(30),
                "editedIntroduction");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile groupImage = new MockMultipartFile("groupImage", "group.jpg", "image/jpeg",
                "image".getBytes());
        doThrow(new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다.")).when(
                        studyGroupService)
                .editGroup(any(User.class), any(EditGroupRequest.class), any(MultipartFile.class));
        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(groupImage)
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("존재하지 않는 그룹 입니다."));

        verify(studyGroupService, times(1)).editGroup(any(User.class), any(EditGroupRequest.class),
                any(MultipartFile.class));
    }

    @Test
    @DisplayName("그룹 정보 수정 실패 : 권한 없음")
    void editGroupFailed_3() throws Exception {
        // given
        EditGroupRequest request = new EditGroupRequest(groupId, "name", LocalDate.now(), LocalDate.now().plusDays(30),
                "editedIntroduction");
        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile groupImage = new MockMultipartFile("groupImage", "group.jpg", "image/jpeg",
                "image".getBytes());
        doThrow(new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "그룹 정보 수정에 대한 권한이 없습니다.")).when(
                        studyGroupService)
                .editGroup(any(User.class), any(EditGroupRequest.class), any(MultipartFile.class));
        // when, then
        mockMvc.perform(multipart("/api/group")
                        .file(requestPart)
                        .file(groupImage)
                        .header("Authorization", token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request1 -> {
                            request1.setMethod("PATCH");
                            return request1;
                        }))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("그룹 정보 수정에 대한 권한이 없습니다."));

        verify(studyGroupService, times(1)).editGroup(any(User.class), any(EditGroupRequest.class),
                any(MultipartFile.class));
    }

    @Test
    @DisplayName("그룹 회원 목록 조회 성공")
    void getGroupInfo() throws Exception {
        // given
        List<GetGroupMemberResponse> response = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            response.add(new GetGroupMemberResponse(
                    "name" + i, LocalDate.now(), "70%", RoleOfGroupMember.ADMIN, "profileImage" + i, (long) i
            ));
        }
        when(studyGroupService.getGroupMemberList(user, groupId)).thenReturn(response);
        // when, then
        mockMvc.perform(get("/api/group/member-list")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(studyGroupService, times(1)).getGroupMemberList(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 회원 목록 조회 실패 : 존재하지 않는 그룹")
    void getGroupInfoFailed_1() throws Exception {
        // given
        when(studyGroupService.getGroupMemberList(user, groupId)).thenThrow(
                new CannotFoundGroupException("그룹을 찾을 수 없습니다."));
        // when, then
        mockMvc.perform(get("/api/group/member-list")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("그룹을 찾을 수 없습니다."));

        verify(studyGroupService, times(1)).getGroupMemberList(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 회원 목록 조회 실패 : 권한 없음")
    void getGroupInfoFailed_2() throws Exception {
        // given
        when(studyGroupService.getGroupMemberList(user, groupId)).thenThrow(
                new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "그룹 내용을 확인할 권한이 없습니다"));
        // when, then
        mockMvc.perform(get("/api/group/member-list")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("그룹 내용을 확인할 권한이 없습니다"));

        verify(studyGroupService, times(1)).getGroupMemberList(any(User.class), anyLong());
    }

    @Test
    @DisplayName("문제 별 회원 풀이 여부 조회 성공")
    void getCheckingSolvedProblem() throws Exception {
        // given
        List<CheckSolvedProblemResponse> response = new ArrayList<>(30);
        for (int i = 0; i < 30; i++) {
            response.add(new CheckSolvedProblemResponse((long) i, "nickname" + i, "profileImage" + i, true));
        }
        when(studyGroupService.getCheckingSolvedProblem(any(User.class), anyLong())).thenReturn(response);
        // when, then
        mockMvc.perform(get("/api/group/problem-solving")
                        .header("Authorization", token)
                        .param("problemId", String.valueOf(problemId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
        verify(studyGroupService, times(1)).getCheckingSolvedProblem(any(User.class), anyLong());
    }

    @Test
    @DisplayName("문제 별 회원 풀이 여부 조회 실패 : 존재하지 않는 문제")
    void getCheckingSolvedProblemFailed_1() throws Exception {
        // given
        when(studyGroupService.getCheckingSolvedProblem(any(User.class), anyLong())).thenThrow(
                new CannotFoundGroupException("문제를 찾을 수 없습니다."));
        // when, then
        mockMvc.perform(get("/api/group/problem-solving")
                        .header("Authorization", token)
                        .param("problemId", String.valueOf(problemId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("문제를 찾을 수 없습니다."));
        verify(studyGroupService, times(1)).getCheckingSolvedProblem(any(User.class), anyLong());
    }

    @Test
    @DisplayName("문제 별 회원 풀이 여부 조회 실패 : 권한 없음")
    void getCheckingSolvedProblemFailed_2() throws Exception {
        // given
        when(studyGroupService.getCheckingSolvedProblem(any(User.class), anyLong())).thenThrow(
                new UserValidationException("풀이 여부 목록을 확인할 권한이 없습니다."));
        // when, then
        mockMvc.perform(get("/api/group/problem-solving")
                        .header("Authorization", token)
                        .param("problemId", String.valueOf(problemId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("풀이 여부 목록을 확인할 권한이 없습니다."));
        verify(studyGroupService, times(1)).getCheckingSolvedProblem(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 초대 코드 조회 성공")
    void getGroupCode() throws Exception {
        // given
        when(studyGroupService.getGroupCode(user, groupId)).thenReturn(code);
        // when, then
        mockMvc.perform(get("/api/group/group-code")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().string(code));
        verify(studyGroupService, times(1)).getGroupCode(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 초대 코드 조회 실패 : 존재하지 않는 그룹")
    void getGroupCodeFailed_1() throws Exception {
        // given
        when(studyGroupService.getGroupCode(user, groupId)).thenThrow(new CannotFoundGroupException("그룹을 찾지 못했습니다."));
        // when, then
        mockMvc.perform(get("/api/group/group-code")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("그룹을 찾지 못했습니다."));
        verify(studyGroupService, times(1)).getGroupCode(any(User.class), anyLong());
    }

    @Test
    @DisplayName("그룹 초대 코드 조회 실패 : 권한 없음")
    void getGroupCodeFailed_2() throws Exception {
        // given
        when(studyGroupService.getGroupCode(user, groupId)).thenThrow(
                new UserValidationException("초대 코드를 조회할 권한이 없습니다."));
        // when, then
        mockMvc.perform(get("/api/group/group-code")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("초대 코드를 조회할 권한이 없습니다."));
        verify(studyGroupService, times(1)).getGroupCode(any(User.class), anyLong());
    }

    @Test
    @DisplayName("스터디 그룹 즐겨찾기 추가 성공")
    void updateBookmarked_1() throws Exception {
        // given
        when(studyGroupService.updateBookmarkGroup(user, groupId)).thenReturn("스터디 그룹 즐겨찾기 추가 성공");
        // when, then
        mockMvc.perform(post("/api/group/bookmark")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().string("스터디 그룹 즐겨찾기 추가 성공"));
        verify(studyGroupService, times(1)).updateBookmarkGroup(user, groupId);
    }

    @Test
    @DisplayName("스터디 그룹 즐겨찾기 삭제 성공")
    void updateBookmarked_2() throws Exception {
        // given
        when(studyGroupService.updateBookmarkGroup(user, groupId)).thenReturn("스터디 그룹 즐겨찾기 실패 성공");
        // when, then
        mockMvc.perform(post("/api/group/bookmark")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isOk())
                .andExpect(content().string("스터디 그룹 즐겨찾기 실패 성공"));
        verify(studyGroupService, times(1)).updateBookmarkGroup(user, groupId);
    }

    @Test
    @DisplayName("스터디 그룹 즐겨찾기 추가/삭제 실패 : 존재하지 않는 그룹")
    void updateBookmarkedFailed_1() throws Exception {
        // given
        when(studyGroupService.updateBookmarkGroup(user, groupId)).thenThrow(
                new CannotFoundSolutionException("존재하지 않는 그룹 입니다."));
        // when, then
        mockMvc.perform(post("/api/group/bookmark")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("존재하지 않는 그룹 입니다."));
        verify(studyGroupService, times(1)).updateBookmarkGroup(user, groupId);
    }

    @Test
    @DisplayName("스터디 그룹 즐겨찾기 추가/삭제 실패 : 참여하지 않은 그룹")
    void updateBookmarkedFailed_2() throws Exception {
        // given
        when(studyGroupService.updateBookmarkGroup(user, groupId)).thenThrow(
                new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));
        // when, then
        mockMvc.perform(post("/api/group/bookmark")
                        .header("Authorization", token)
                        .param("groupId", String.valueOf(groupId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("참여하지 않은 그룹 입니다."));
        verify(studyGroupService, times(1)).updateBookmarkGroup(user, groupId);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 역할 수정 성공")
    void updateGroupMemberRole() throws Exception {
        // given
        UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 20L, "ADMIN");
        // when, then
        mockMvc.perform(patch("/api/group/role")
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
        verify(studyGroupService, times(1)).updateGroupMemberRole(user, request);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 역할 수정 실패 : 존재하지 않는 그룹")
    void updateGroupMemberRoleFailed_1() throws Exception {
        // given
        UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 20L, "ADMIN");
        doThrow(new CannotFoundSolutionException("존재하지 않는 그룹입니다.")).when(studyGroupService)
                .updateGroupMemberRole(user, request);
        // when, then
        mockMvc.perform(patch("/api/group/role")
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("존재하지 않는 그룹입니다."));
        verify(studyGroupService, times(1)).updateGroupMemberRole(user, request);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 역할 수정 실패 : 스터디 그룹 멤버 역할 수정 권한 없음")
    void updateGroupMemberRoleFailed_2() throws Exception {
        // given
        UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 20L, "ADMIN");
        doThrow(new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "스터디 그룹 멤버 역할을 수정할 권한이 없습니다.")).when(
                studyGroupService).updateGroupMemberRole(user, request);
        // when, then
        mockMvc.perform(patch("/api/group/role")
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("스터디 그룹 멤버 역할을 수정할 권한이 없습니다."));
        verify(studyGroupService, times(1)).updateGroupMemberRole(user, request);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 역할 수정 실패 : 존재하지 않는 회원")
    void updateGroupMemberRoleFailed_3() throws Exception {
        // given
        UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 20L, "ADMIN");
        doThrow(new UserValidationException("존재하지 않는 회원입니다.")).when(
                studyGroupService).updateGroupMemberRole(user, request);
        // when, then
        mockMvc.perform(patch("/api/group/role")
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("존재하지 않는 회원입니다."));
        verify(studyGroupService, times(1)).updateGroupMemberRole(user, request);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 역할 수정 실패 : 스터디 그룹에 참여하지 않은 회원")
    void updateGroupMemberRoleFailed_4() throws Exception {
        // given
        UpdateGroupMemberRoleRequest request = new UpdateGroupMemberRoleRequest(groupId, 20L, "ADMIN");
        doThrow(new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "해당 스터디 그룹에 참여하지 않은 회원입니다.")).when(
                studyGroupService).updateGroupMemberRole(user, request);
        // when, then
        mockMvc.perform(patch("/api/group/role")
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("해당 스터디 그룹에 참여하지 않은 회원입니다."));
        verify(studyGroupService, times(1)).updateGroupMemberRole(user, request);
    }
}