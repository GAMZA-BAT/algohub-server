package com.gamzabat.algohub.feature.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.board.domain.Board;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

public interface BoardRepository extends JpaRepository<Board, Long> {
	List<Board> findAllByStudyGroup(StudyGroup studyGroup);

}
