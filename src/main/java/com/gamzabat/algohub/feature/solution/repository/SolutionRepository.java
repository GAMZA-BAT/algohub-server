package com.gamzabat.algohub.feature.solution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.repository.querydsl.CustomSolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;

public interface SolutionRepository extends JpaRepository<Solution, Long>, CustomSolutionRepository {
	Boolean existsByUserAndProblem(User user, Problem problem);

	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId")
	Integer countDistinctUsersByProblemId(@Param("problemId") Long problemId);

	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId AND s.result = :correct")
	Integer countDistinctUsersWithCorrectSolutionsByProblemId(@Param("problemId") Long problemId,
		@Param("correct") String correct);

	@Query("SELECT COUNT(DISTINCT s.problem.id) FROM Solution s " +
		"JOIN s.problem p " +
		"WHERE s.user = :user " +
		"AND p.studyGroup.id = :groupId " +
		"AND s.result = :correct")
	Long countDistinctCorrectSolutionsByUserAndGroup(@Param("user") User user, @Param("groupId") Long groupId,
		@Param("correct") String correct);

	boolean existsByUserAndProblemAndResult(User user, Problem problem, String result);
}
