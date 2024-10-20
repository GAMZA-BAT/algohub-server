package com.gamzabat.algohub.feature.board.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.dto.GetBoardResponse;
import com.gamzabat.algohub.feature.board.exception.BoardValidationExceoption;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor

public class BoardService {
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;

	@Transactional
	public void createBoard(@AuthedUser User user, CreateBoardRequest request) {
		StudyGroup studyGroup = studyGroupRepository.findById(request.studyGroupId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다"));
		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(
				() -> new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isParticipant(groupMember))
			throw new UserValidationException("공지 작성 권한이 없습니다");

		boardRepository.save(Board.builder()
			.author(user)
			.studyGroup(studyGroup)
			.title(request.title())
			.content(request.content())
			.createdAt(LocalDateTime.now())
			.build());
		log.info("success to create board");
	}

	@Transactional(readOnly = true)
	public GetBoardResponse getBoard(@AuthedUser User user, Long boardId) {
		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new BoardValidationExceoption("존재하지 않는 공지입니다"));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, board.getStudyGroup()))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹 입니다.");

		log.info("success to get board");
		return GetBoardResponse.builder()
			.author(board.getAuthor().getNickname())
			.boardId(board.getId())
			.boardTitle(board.getTitle())
			.boardContent(board.getContent())
			.createAt(board.getCreatedAt())
			.build();
	}

	@Transactional(readOnly = true)
	public List<GetBoardResponse> getBoardList(@AuthedUser User user, Long studyGroupId) {
		StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다"));
		if (!groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹입니다");

		List<Board> list = boardRepository.findAllByStudyGroup(studyGroup);
		List<GetBoardResponse> result = list.stream().map(GetBoardResponse::toDTO).toList();
		log.info("success to get board list");
		return result;
	}

}
