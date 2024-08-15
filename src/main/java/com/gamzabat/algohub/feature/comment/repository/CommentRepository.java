package com.gamzabat.algohub.feature.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment,Long> {
	List<Comment> findAllBySolution(Solution solution);
	@Query("SELECT COUNT(c) FROM Comment c WHERE c.solution.id = :solutionId")
	long countCommentsBySolutionId(@Param("solutionId") Long solutionId);
}
