package com.gamzabat.algohub.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gamzabat.algohub.feature.comment.service.CommentService;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
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

import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.comment.dto.CreateCommentRequest;
import com.gamzabat.algohub.feature.comment.dto.GetCommentResponse;
import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.exception.ProblemValidationException;
import com.gamzabat.algohub.feature.comment.exception.SolutionValidationException;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.feature.comment.repository.CommentRepository;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
	@InjectMocks
	private CommentService commentService;
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
	private NotificationRepository notificationRepository;
	private User user, user2;
	private Comment comment, comment2;
	private Solution solution;
	private Problem problem;
	private StudyGroup studyGroup;
	@Captor
	private ArgumentCaptor<Comment> commentCaptor;
	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname")
			.role(Role.USER).profileImage("image").build();
		studyGroup = StudyGroup.builder().owner(user).build();
		problem = Problem.builder().studyGroup(studyGroup).build();
		solution = Solution.builder().problem(problem).user(user).content("solution").build();
		comment = Comment.builder().user(user).content("content").solution(solution).build();
		comment2 = Comment.builder().user(user2).content("content").solution(solution).build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user,1L);
		userField.set(user2,2L);

		Field solutionField = Solution.class.getDeclaredField("id");
		solutionField.setAccessible(true);
		solutionField.set(solution,10L);

		Field problemField = Problem.class.getDeclaredField("id");
		problemField.setAccessible(true);
		problemField.set(problem,20L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(studyGroup,30L);

		Field commentField = Comment.class.getDeclaredField("id");
		commentField.setAccessible(true);
		commentField.set(comment,40L);
		commentField.set(comment2,41L);
	}

	 @Test
	 @DisplayName("댓글 작성 성공 (주인)")
	 void createComment() {
	 	// given
	 	CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
	 	when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
	 	when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
	 	when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
	 	// when
	 	commentService.createComment(user,request);
	 	// then
	 	verify(commentRepository,times(1)).save(commentCaptor.capture());
	 	Comment result = commentCaptor.getValue();
	 	assertThat(result.getContent()).isEqualTo("content");
	 	assertThat(result.getUser()).isEqualTo(user);
	 	assertThat(result.getSolution()).isEqualTo(solution);
		verify(notificationService, times(1)).send(any(),any(),any(),any());
	 }

	 @Test
	 @DisplayName("댓글 작성 성공 (멤버)")
	 void createComment_2() {
	 	// given
	 	CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
	 	when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
	 	when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
	 	when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
	 	when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(true);
	 	// when
	 	commentService.createComment(user2,request);
	 	// then
	 	verify(commentRepository,times(1)).save(commentCaptor.capture());
	 	Comment result = commentCaptor.getValue();
	 	assertThat(result.getContent()).isEqualTo("content");
	 	assertThat(result.getUser()).isEqualTo(user2);
	 	assertThat(result.getSolution()).isEqualTo(solution);
		 verify(notificationService, times(1)).send(any(),any(),any(),any());
	 }

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 풀이")
	void createCommentFailed_1(){
		// given
		CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
		when(solutionRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user,request))
			.isInstanceOf(SolutionValidationException.class)
			.hasFieldOrPropertyWithValue("error","존재하지 않는 풀이 입니다.");
		verify(notificationService, never()).send(any(),any(),any(),any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 문제")
	void createCommentFailed_2(){
		// given
		CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user,request))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 문제 입니다.");
		verify(notificationService, never()).send(any(),any(),any(),any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 존재하지 않는 그룹")
	void createCommentFailed_3(){
		// given
		CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user,request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
		verify(notificationService, never()).send(any(),any(),any(),any());
	}

	@Test
	@DisplayName("댓글 작성 실패 : 참여하지 않은 그룹")
	void createCommentFailed_4(){
		// given
		CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.createComment(user2,request))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","참여하지 않은 그룹 입니다.");
		verify(notificationService, never()).send(any(),any(),any(),any());
	}

	@Test
	@DisplayName("댓글 작성 성공, 알림 전송 실패")
	void createCommentSuccess_NotificationFailed(){
		// given
		CreateCommentRequest request = CreateCommentRequest.builder().solutionId(10L).content("content").build();
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(true);
		doThrow(new RuntimeException()).when(notificationService).send(any(),any(),any(),any());
		// when
		commentService.createComment(user2,request);
		// then
		verify(commentRepository,times(1)).save(any(Comment.class));
		verify(notificationService, times(1)).send(any(),any(),any(),any());
		verify(notificationRepository, never()).save(any());
	}

	@Test
	@DisplayName("댓글 조회 성공 (주인)")
	void getCommentList_1() {
		// given
		List<Comment> list = new ArrayList<>(30);
		for(int i=0; i<30; i++)
			list.add(Comment.builder().solution(solution).user(user).content("content"+i).build());
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(commentRepository.findAllBySolution(solution)).thenReturn(list);
		// when
		List<GetCommentResponse> result = commentService.getCommentList(user, 10L);
		// then
		assertThat(result.size()).isEqualTo(30);
		for(int i=0; i<30; i++)
			assertThat(result.get(i).content()).isEqualTo("content"+i);
	}

	@Test
	@DisplayName("댓글 조회 성공 (멤버)")
	void getComment_2() {
		// given
		List<Comment> list = new ArrayList<>(30);
		for(int i=0; i<30; i++)
			list.add(Comment.builder().solution(solution).user(user).content("content"+i).build());
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(true);
		when(commentRepository.findAllBySolution(solution)).thenReturn(list);
		// when
		List<GetCommentResponse> result = commentService.getCommentList(user2, 10L);
		// then
		assertThat(result.size()).isEqualTo(30);
		for(int i=0; i<30; i++)
			assertThat(result.get(i).content()).isEqualTo("content"+i);
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 풀이")
	void getCommentListFailed_1(){
		// given
		when(solutionRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user,10L))
			.isInstanceOf(SolutionValidationException.class)
			.hasFieldOrPropertyWithValue("error","존재하지 않는 풀이 입니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 문제")
	void getCommentListFailed_2(){
		// given
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user,10L))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 존재하지 않는 그룹")
	void getCommentListFailed_3(){
		// given
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user,10L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 조회 실패 : 참여하지 않은 그룹")
	void getCommentListFailed_4(){
		// given
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.getCommentList(user2,10L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 성공 (주인)")
	void deleteComment_1() {
		// given
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		// when
		commentService.deleteComment(user, 40L);
		// then
		verify(commentRepository,times(1)).delete(comment);
	}

	@Test
	@DisplayName("댓글 삭제 성공 (멤버)")
	void deleteComment_2() {
		// given
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(true);
		when(commentRepository.findById(41L)).thenReturn(Optional.ofNullable(comment2));
		// when
		commentService.deleteComment(user2, 41L);
		// then
		verify(commentRepository,times(1)).delete(comment2);
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 댓글")
	void deleteCommentFailed_1(){
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user,40L))
			.isInstanceOf(CommentValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 댓글 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 댓글 삭제 권한 없음")
	void deleteCommentFailed_2(){
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user2,40L))
			.isInstanceOf(CommentValidationException.class)
			.hasFieldOrPropertyWithValue("code",HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","댓글 삭제에 대한 권한이 없습니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 풀이")
	void deleteCommentFailed_3(){
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		when(solutionRepository.findById(10L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user,40L))
			.isInstanceOf(SolutionValidationException.class)
			.hasFieldOrPropertyWithValue("error","존재하지 않는 풀이 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 문제")
	void deleteCommentFailed_4(){
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user,40L))
			.isInstanceOf(ProblemValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 문제 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 존재하지 않는 그룹")
	void deleteCommentFailed_5(){
		// given
		when(commentRepository.findById(40L)).thenReturn(Optional.ofNullable(comment));
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.empty());
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user,40L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value())
			.hasFieldOrPropertyWithValue("error","존재하지 않는 그룹 입니다.");
	}

	@Test
	@DisplayName("댓글 삭제 실패 : 참여하지 않은 그룹")
	void deleteCommentFailed_6(){
		// given
		when(commentRepository.findById(41L)).thenReturn(Optional.ofNullable(comment2));
		when(solutionRepository.findById(10L)).thenReturn(Optional.ofNullable(solution));
		when(problemRepository.findById(20L)).thenReturn(Optional.ofNullable(problem));
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2,studyGroup)).thenReturn(false);
		// when, then
		assertThatThrownBy(() -> commentService.deleteComment(user2,41L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error","참여하지 않은 그룹 입니다.");
	}

}