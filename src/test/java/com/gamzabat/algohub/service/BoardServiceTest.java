package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
import com.gamzabat.algohub.feature.board.service.BoardService;
import com.gamzabat.algohub.feature.comment.repository.CommentRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {
	@InjectMocks
	private BoardService boardService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private CommentRepository commentRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private SolutionRepository solutionRepository;
	@Mock
	private ProblemRepository problemRepository;
	@Mock
	private BoardRepository boardRepository;
	@Captor
	private ArgumentCaptor<Board> boardCaptor;

	private User user, user2;
	private StudyGroup studyGroup;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		studyGroup = StudyGroup.builder().owner(user).build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(studyGroup, 30L);

	}

	@Test
	@DisplayName("게시판 작성 성공")
	void createBoardSuccess() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(30L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.ofNullable(studyGroup));

		//when
		boardService.createBoard(user, request);
		//then
		verify(boardRepository, times(1)).save(boardCaptor.capture());
		Board result = boardCaptor.getValue();
		assertThat(result.getUser()).isEqualTo(user);
		assertThat(result.getContent()).isEqualTo("content");
		assertThat(result.getTitle()).isEqualTo("title");

	}

	@Test
	@DisplayName("게시판 작성 실패 그룹장이 아님")
	void createBoardFail_1() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(30L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.ofNullable(studyGroup));

		//when,then
		assertThatThrownBy(() -> boardService.createBoard(user2, request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "게시글 작성 권한이 없습니다");

	}

	@Test
	@DisplayName("게시판 작성 실패 존재하지 않는 그룹")
	void createBoardFail_2() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(31L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> boardService.createBoard(user2, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.extracting("code", "error")
			.containsExactly(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다");
	}

}
