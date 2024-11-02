package com.gamzabat.algohub.feature.solution.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.domain.SolutionComment;

public interface SolutionCommentRepository extends JpaRepository<SolutionComment, Long> {
	List<SolutionComment> findAllBySolution(Solution solution);

	@Query("SELECT COUNT(c) FROM SolutionComment c WHERE c.solution.id = :solutionId")
	long countCommentsBySolutionId(@Param("solutionId") Long solutionId);
}
