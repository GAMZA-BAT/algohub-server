package com.gamzabat.algohub.feature.solution.repository;

import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.user.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolutionRepository extends JpaRepository<Solution,Long> {
	Page<Solution> findAllByProblemOrderBySolvedDateTimeDesc(Problem problem, Pageable pageable);
	Boolean existsByUserAndProblem(User user, Problem problem);


	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId")
	Integer countDistinctUsersByProblemId(@Param("problemId") Long problemId);

	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId AND s.isCorrect = true")
	Integer countDistinctUsersWithCorrectSolutionsByProblemId(@Param("problemId") Long problemId);

	@Query("SELECT COUNT(DISTINCT s.problem.id) FROM Solution s " +
			"JOIN s.problem p " +
			"WHERE s.user = :user " +
			"AND p.studyGroup.id = :groupId " +
			"AND s.isCorrect = true")
	Long countDistinctCorrectSolutionsByUserAndGroup(@Param("user") User user, @Param("groupId") Long groupId);

	@Query("SELECT new com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse(u.nickname, u.profileImage, 0, COUNT(DISTINCT s.problem.id)) " +
			"FROM Solution s " +
			"JOIN s.user u " +
			"JOIN s.problem p " +
			"JOIN p.studyGroup g " +
			"WHERE s.isCorrect = true AND g = :group " +
			"GROUP BY u.id, u.nickname, u.profileImage " +
			"ORDER BY COUNT(DISTINCT s.problem.id) DESC, MAX(s.solvedDateTime) ASC")
	List<GetRankingResponse> findTopUsersByGroup(@Param("group") StudyGroup group);


	boolean existsByUserAndProblemAndIsCorrect(User user, Problem problem, boolean isCorrect);
}
