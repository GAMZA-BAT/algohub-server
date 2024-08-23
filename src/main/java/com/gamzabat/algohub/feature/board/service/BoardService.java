package com.gamzabat.algohub.feature.board.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
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

	public void createBoard(@AuthedUser User user, CreateBoardRequest request) {
		StudyGroup studyGroup = studyGroupRepository.findById(request.studyGroupId())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 스터디 그룹입니다"));
		if (!studyGroup.getOwner().getId().equals(user.getId())) {
			throw new UserValidationException("게시글 작성 권한이 없습니다");
		}
		boardRepository.save(Board.builder()
			.user(user)
			.title(request.title())
			.content(request.content())
			.createdAt(LocalDateTime.now())
			.build());

	}

}
