package com.gamzabat.algohub.feature.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.domain.BoardComment;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	List<BoardComment> findAllByBoard(Board board);
}
