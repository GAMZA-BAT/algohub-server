package com.gamzabat.algohub.service;

import static com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.enums.Role;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.dto.GetBoardResponse;
import com.gamzabat.algohub.feature.board.exception.BoardValidationExceoption;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
import com.gamzabat.algohub.feature.board.service.BoardService;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {
	@InjectMocks
	private BoardService boardService;
	@Mock
	private StudyGroupRepository studyGroupRepository;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	GroupMemberRepository groupMemberRepository;
	@Captor
	private ArgumentCaptor<Board> boardCaptor;

	private User user, user2, user3, user4;
	private StudyGroup studyGroup;
	private GroupMember groupMember, groupMember2, groupMember3;
	private Board board;

	@BeforeEach
	void setUp() throws NoSuchFieldException, IllegalAccessException {
		user = User.builder().email("email1").password("password").nickname("nickname1")
			.role(Role.USER).profileImage("image").build();
		user2 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		user3 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		user4 = User.builder().email("email2").password("password").nickname("nickname2")
			.role(Role.USER).profileImage("image").build();
		studyGroup = StudyGroup.builder().build();
		groupMember2 = GroupMember.builder().user(user2).studyGroup(studyGroup).role(ADMIN).build();
		groupMember3 = GroupMember.builder().user(user3).studyGroup(studyGroup).role(PARTICIPANT).build();
		board = Board.builder().studyGroup(studyGroup).title("title").content("content").author(user).build();

		Field userField = User.class.getDeclaredField("id");
		userField.setAccessible(true);
		userField.set(user, 1L);
		userField.set(user2, 2L);
		userField.set(user3, 3L);
		userField.set(user4, 4L);

		Field groupField = StudyGroup.class.getDeclaredField("id");
		groupField.setAccessible(true);
		groupField.set(studyGroup, 30L);

		Field groupMemberField = GroupMember.class.getDeclaredField("id");
		groupMemberField.setAccessible(true);
		groupMemberField.set(groupMember2, 200L);
		groupMemberField.set(groupMember3, 300L);

		Field boardField = Board.class.getDeclaredField("id");
		boardField.setAccessible(true);
		boardField.set(board, 1000L);

	}

	@Test
	@DisplayName("공지 작성 성공")
	void createBoardSuccess_1() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(30L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.findByUserAndStudyGroup(user2, studyGroup)).thenReturn(
			Optional.ofNullable(groupMember2));
		//when
		boardService.createBoard(user2, request);
		//then
		verify(boardRepository, times(1)).save(boardCaptor.capture());
		Board result = boardCaptor.getValue();
		assertThat(result.getAuthor()).isEqualTo(user2);
		assertThat(result.getContent()).isEqualTo("content");
		assertThat(result.getTitle()).isEqualTo("title");
		assertThat(result.getStudyGroup()).isEqualTo(studyGroup);

	}

	@Test
	@DisplayName("공지 작성 실패 그룹장or부방장이 아님")
	void createBoardFail_1() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(30L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.findByUserAndStudyGroup(user3, studyGroup)).thenReturn(
			Optional.ofNullable(groupMember3));
		//when,then
		assertThatThrownBy(() -> boardService.createBoard(user3, request))
			.isInstanceOf(UserValidationException.class)
			.hasFieldOrPropertyWithValue("errors", "공지 작성 권한이 없습니다");

	}

	@Test
	@DisplayName("공지 작성 실패 존재하지 않는 그룹")
	void createBoardFail_2() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(31L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> boardService.createBoard(user, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.extracting("code", "error")
			.containsExactly(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다");
	}

	@Test
	@DisplayName("공지 작성 실패 존재하지 않는 멤버")
	void createBoardFail_3() {
		//given
		CreateBoardRequest request = new CreateBoardRequest(30L, "title", "content");
		when(studyGroupRepository.findById(request.studyGroupId())).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.findByUserAndStudyGroup(user4, studyGroup)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> boardService.createBoard(user4, request))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 그룹 입니다.");
	}

	@Test
	@DisplayName("공지 조회 성공")
	void getBoardSuccess_1() {
		//given
		when(boardRepository.findById(1000L)).thenReturn(Optional.ofNullable(board));
		when(groupMemberRepository.existsByUserAndStudyGroup(user2, studyGroup)).thenReturn(
			true);
		//when
		GetBoardResponse response = boardService.getBoard(user2, 1000L);
		//then
		assertThat(response.author()).isEqualTo("nickname1");
		assertThat(response.boardContent()).isEqualTo("content");
		assertThat(response.boardTitle()).isEqualTo("title");
		assertThat(response.boardId()).isEqualTo(1000L);
	}

	@Test
	@DisplayName("공지 조회 실패(존재하지 않는 공지)")
	void getBoardFailed_1() {
		//given
		when(boardRepository.findById(1001L)).thenReturn(Optional.empty());

		//when, then
		assertThatThrownBy(() -> boardService.getBoard(user, 1001L))
			.isInstanceOf(BoardValidationExceoption.class)
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 공지입니다");

	}

	@Test
	@DisplayName("공지 조회 실패(그룹에 참여하지 않은 유저)")
	void getBoardFailed_2() {
		//given
		when(boardRepository.findById(1000L)).thenReturn(Optional.ofNullable(board));
		when(groupMemberRepository.existsByUserAndStudyGroup(user4, board.getStudyGroup())).thenReturn(false);
		//when
		assertThatThrownBy(() -> boardService.getBoard(user4, 1000L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 스터디 그룹 입니다.");
	}

	@Test
	@DisplayName("공지 목록 조회 성공")
	void getBoardListSuccess_1() {
		//given
		List<Board> boardList = new ArrayList<>(10);
		for (int i = 0; i < 10; i++)
			boardList.add(
				Board.builder().author(user).content("content" + i).title("title" + i).studyGroup(studyGroup).build());
		for (int i = 10; i < 20; i++)
			boardList.add(
				Board.builder().author(user2).content("content" + i).title("title" + i).studyGroup(studyGroup).build());
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup)).thenReturn(true);
		when(boardRepository.findAllByStudyGroup(studyGroup)).thenReturn(boardList);
		//when
		List<GetBoardResponse> result = boardService.getBoardList(user, 30L);
		//then
		assertThat(result.size()).isEqualTo(20);
		for (int i = 0; i < 20; i++) {
			assertThat(result.get(i).boardContent()).isEqualTo("content" + i);
			assertThat(result.get(i).boardTitle()).isEqualTo("title" + i);
		}
	}

	@Test
	@DisplayName("공지 목록 조회 실패 (존재하지 않는 스터디 그룹임)")
	void getBoardListFailed_1() {
		//given
		when(studyGroupRepository.findById(31L)).thenReturn(Optional.empty());
		//when,then
		assertThatThrownBy(() -> boardService.getBoardList(user, 31L))
			.isInstanceOf(StudyGroupValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.BAD_REQUEST.value())
			.hasFieldOrPropertyWithValue("error", "존재하지 않는 스터디 그룹입니다");
	}

	@Test
	@DisplayName("공지 목록 조회 실패(참여하지 않은 스터디 그룹)")
	void getBoardListFailed_2() {
		//given
		when(studyGroupRepository.findById(30L)).thenReturn(Optional.ofNullable(studyGroup));
		when(groupMemberRepository.existsByUserAndStudyGroup(user4, studyGroup)).thenReturn(false);
		//when, then
		assertThatThrownBy(() -> boardService.getBoardList(user4, 30L))
			.isInstanceOf(GroupMemberValidationException.class)
			.hasFieldOrPropertyWithValue("code", HttpStatus.FORBIDDEN.value())
			.hasFieldOrPropertyWithValue("error", "참여하지 않은 스터디 그룹입니다");
	}

}
