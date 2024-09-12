package com.gamzabat.algohub.feature.board.service;

import java.time.LocalDateTime;
import java.util.Optional;

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
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
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
		Optional<GroupMember> groupMember = groupMemberRepository.findByUserAndStudyGroup(user, board.getStudyGroup());

		Boolean isOwner = (board.getStudyGroup().getOwner().getId().equals(user.getId()) && groupMember.isEmpty());
		Boolean isGroupMember = groupMember.isPresent();

		if (!isGroupMember && !isOwner)
			throw new UserValidationException("공지를 조회할 권한이 없습니다");

		log.info("success to get board");
		return GetBoardResponse.builder()
			.author(board.getAuthor().getNickname())
			.boardId(board.getId())
			.boardTitle(board.getTitle())
			.boardContent(board.getContent())
			.createAt(board.getCreatedAt())
			.build();
	}

}
