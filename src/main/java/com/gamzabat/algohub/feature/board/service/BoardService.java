package com.gamzabat.algohub.feature.board.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.dto.CreateBoardRequest;
import com.gamzabat.algohub.feature.board.repository.BoardRepository;
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
	
	public void createBoard(@AuthedUser User user, CreateBoardRequest request) {

		boardRepository.save(Board.builder()
			.user(user)
			.title(request.title())
			.content(request.content())
			.createdAt(LocalDateTime.now())
			.build());

	}

}
