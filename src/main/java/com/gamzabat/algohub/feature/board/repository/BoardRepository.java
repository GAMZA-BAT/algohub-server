package com.gamzabat.algohub.feature.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.board.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
	Optional<Board> findById(Long id);

}
