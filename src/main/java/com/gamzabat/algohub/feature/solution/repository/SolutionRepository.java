package com.gamzabat.algohub.feature.solution.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.repository.querydsl.CustomSolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse;
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

	@Query(
		"SELECT new com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse(u.nickname, u.profileImage, 0, COUNT(DISTINCT s.problem.id)) "
			+
			"FROM Solution s " +
			"JOIN s.user u " +
			"JOIN s.problem p " +
			"JOIN p.studyGroup g " +
			"WHERE s.result = :correct AND g = :group " +
			"GROUP BY u.id, u.nickname, u.profileImage " +
			"ORDER BY COUNT(DISTINCT s.problem.id) DESC, MAX(s.solvedDateTime) ASC")
	List<GetRankingResponse> findTopUsersByGroup(@Param("group") StudyGroup group, @Param("correct") String correct);

	boolean existsByUserAndProblemAndResult(User user, Problem problem, String result);
}
