package com.gamzabat.algohub.feature.board.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.domain.BoardComment;
import com.gamzabat.algohub.feature.board.dto.CreateBoardCommentRequest;
import com.gamzabat.algohub.feature.board.exception.BoardValidationExceoption;
import com.gamzabat.algohub.feature.board.repository.BoardCommentRepository;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.feature.comment.dto.UpdateCommentRequest;
import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
class BoardCommentServiceTest {
	@InjectMocks
	private BoardCommentService commentService;
	@Mock
	private NotificationService notificationService;
	@Mock
	private BoardCommentRepository commentRepository;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private GroupMemberRepository groupMemberRepository;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private NotificationRepository notificationRepository;
	private User user, user2;
	private BoardComment comment, comment2;
	private Board board;
	private StudyGroup studyGroup;
	@Captor
	private ArgumentCaptor<BoardComment> commentCaptor;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		studyGroup = StudyGroup.builder().build();
		board = Board.builder().author(user).studyGroup(studyGroup).content("board content.").build();
		comment = BoardComment.builder().user(user).content("content").board(board).build();
		comment2 = BoardComment.builder().user(user2).content("content").board(board).build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);

		Field boardField = Board.class.getDeclaredField("id");
		boardField.setAccessible(true);
		boardField.set(board, 10L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(studyGroup, 30L);

		Field commentField = Comment.class.getDeclaredField("id");
		commentField.setAccessible(true);
		commentField.set(comment, 40L);
		commentField.set(comment2, 41L);
	}

	@Test
	@DisplayName("댓글 작성 성공")
	void createComment_1() {

		CreateBoardCommentRequest request = CreateBoardCommentRequest.builder()
			.boardId(10L)
			.content("content")
			.build();
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.save(any(BoardComment.class))).thenReturn(comment);
		
		// when
		commentService.createComment(user2, request);
		// then
		verify(commentRepository, times(1)).save(commentCaptor.capture());
		BoardComment result = commentCaptor.getValue();
		assertThat(result.getContent()).isEqualTo("content");
		assertThat(result.getUser()).isEqualTo(user2);
		assertThat(result.getBoard()).isEqualTo(board);
		verify(notificationService, times(1)).send(any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 공지")
	void createCommentFailed_1() {
		// given
		CreateBoardCommentRequest request = CreateBoardCommentRequest.builder()
			.boardId(10L)
			.content("content")
			.build();
		when(boardRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user, request))
			.isInstanceOf(BoardValidationExceoption.class)
			.hasFieldOrPropertyWithValue("error", "공지사항이 존재하지 않습니다.");
		verify(notificationService, never()).send(any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 그룹")
	void createCommentFailed_2() {
		// given
		CreateBoardCommentRequest request = CreateBoardCommentRequest.builder()
			.boardId(10L)
			.content("content")
			.build();
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹이 존재하지 않습니다.");
		verify(notificationService, never()).send(any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 참여하지 않은 그룹")
	void createCommentFailed_3() {
		// given
		CreateBoardCommentRequest request = CreateBoardCommentRequest.builder()
			.boardId(10L)
			.content("content")
			.build();
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user2, request))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
		verify(notificationService, never()).send(any(), any(), any(), any());
	}

	@Test
	@DisplayName("댓글 작성 성공, 알림 전송 실패")
	void createCommentSuccess_NotificationFailed() {
		// given
		CreateBoardCommentRequest request = CreateBoardCommentRequest.builder()
			.boardId(10L)
			.content("content")
			.build();
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.save(any(BoardComment.class))).thenReturn(comment);
		doThrow(new RuntimeException()).when(notificationService).send(any(), any(), any(), any());
		// when
		commentService.createComment(user2, request);
		// then
		verify(commentRepository, times(1)).save(any(BoardComment.class));
		verify(notificationService, times(1)).send(any(), any(), any(), any());
		verify(notificationRepository, never()).save(any());
	}

	@Test
	@DisplayName("댓글 조회 성공")
	void getComment_1() {
		// given
		List<BoardComment> list = new ArrayList<>(30);
		for (int i = 0; i < 30; i++)
			list.add(BoardComment.builder()
				.board(board)
				.createdAt(LocalDateTime.now())
				.user(user)
				.content("content" + i)
				.build());
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.findAllByBoard(board)).thenReturn(list);
		// when
		List<GetCommentResponse> result = commentService.getCommentList(user2, 10L);
		// then
		assertThat(result.size()).isEqualTo(30);
		for (int i = 0; i < 30; i++)
			assertThat(result.get(i).content()).isEqualTo("content" + i);
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 공지")
	void getCommentListFailed_1() {
		// given
		when(boardRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user, 10L))
			.isInstanceOf(BoardValidationExceoption.class)
			.hasFieldOrPropertyWithValue("error", "공지사항이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 그룹")
	void getCommentListFailed_2() {
		// given
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user, 10L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 참여하지 않은 그룹")
	void getCommentListFailed_3() {
		// given
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user2, 10L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 성공")
	void deleteComment_1() {
		// given
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(true);
		when(commentRepository.findById(41L)).thenReturn(Optional.ofNullable(comment2));
		// when
		commentService.deleteComment(user2, 41L);
		// then
		verify(commentRepository, times(1)).delete(comment2);
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 댓글")
	void deleteCommentFailed_1() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user, 40L))
			.isInstanceOf(CommentValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "댓글이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 댓글 삭제 권한 없음")
	void deleteCommentFailed_2() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user2, 40L))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "댓글 작성자만 삭제할 수 있습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 공지")
	void deleteCommentFailed_3() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user, 40L))
			.isInstanceOf(BoardValidationExceoption.class)
			.hasFieldOrPropertyWithValue("error", "공지사항이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 그룹")
	void deleteCommentFailed_4() {
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user, 40L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error", "스터디 그룹이 존재하지 않습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 참여하지 않은 그룹")
	void deleteCommentFailed_5() {
		// given
		when(commentRepository.findById(41L)).thenReturn(Optional.ofNullable(comment2));
		when(boardRepository.findById(10L)).thenReturn(Optional.ofNullable(board));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user2, 41L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 수정 성공")
	void testUpdateCommentSuccess() {
		// given
		UpdateCommentRequest request = new UpdateCommentRequest(40L, "Updated content");
		when(commentRepository.findById(request.commentId())).thenReturn(Optional.of(comment));
		LocalDateTime previousUpdatedAt = comment.getUpdatedAt();

		// 현재 시간을 모킹
		LocalDateTime fixedNow = LocalDateTime.of(2024, 8, 23, 12, 0);
		try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
			mockedStatic.when(LocalDateTime::now).thenReturn(fixedNow);

			// when
			commentService.updateComment(user, request);

			// then
			verify(commentRepository).findById(request.commentId());
			assertEquals("Updated content", comment.getContent());
			assertEquals(fixedNow, comment.getUpdatedAt());  // 모킹한 시간으로 검증

		}
	}

	@Test
	@DisplayName("댓글 수정 실패(작성자가 아님)")
	void testUpdateCommentFailed_1() {
		//given
		UpdateCommentRequest request = new UpdateCommentRequest(40L, "Updated content");
		when(commentRepository.findById(request.commentId())).thenReturn(Optional.ofNullable(comment));
		//when,then
		assertThatThrownBy(() -> commentService.updateComment(user2, request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "댓글 작성자만 수정할 수 있습니다.");

	}

	@Test
	@DisplayName("댓글 수정 실패(존재하지 않는 댓글)")
	void testUpdateCommentFailed_2() {
		//given
		UpdateCommentRequest request = new UpdateCommentRequest(50L, "Updated content");
		when(commentRepository.findById(request.commentId())).thenReturn(Optional.empty());
		//when, then
		assertThatThrownBy(() -> commentService.updateComment(user2, request))
			.isInstanceOf(CommentValidationException.class)
			.extracting("code", "error")
			.containsExactly(HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다.");
	}
}