package com.gamzabat.algohub.feature.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.board.domain.BoardComment;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

	List<BoardComment> findAllByBoard(Board board);

	@Modifying
	@Query("delete from BoardComment c where c.board = :board")
	void deleteAllCommentByBoard(Board board);
}
