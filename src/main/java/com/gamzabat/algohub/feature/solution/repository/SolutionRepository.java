package com.gamzabat.algohub.feature.solution.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.repository.querydsl.CustomSolutionRepository;
import com.gamzabat.algohub.feature.user.domain.User;

public interface SolutionRepository extends JpaRepository<Solution, Long>, CustomSolutionRepository {
	@Query("SELECT s FROM Solution s "
		+ "WHERE s.id = :id "
		+ "AND s.deletedAt IS NULL")
	Optional<Solution> findById(Long id);

	Boolean existsByUserAndProblem(User user, Problem problem);

	@Query("SELECT COUNT(DISTINCT s.user) "
		+ "FROM Solution s "
		+ "WHERE s.problem = :problem")
	Integer countDistinctUsersByProblem(Problem problem);

	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId AND s.result = :correct")
	Integer countDistinctUsersWithCorrectSolutionsByProblemId(Long problemId,
		String correct);

	@Query("SELECT COUNT(DISTINCT s.problem.id) FROM Solution s " +
		"JOIN s.problem p " +
		"WHERE s.user = :user " +
		"AND p.studyGroup = :group " +
		"AND s.result = :correct")
	Long countDistinctCorrectSolutionsByUserAndGroup(User user, StudyGroup group,
		String correct);

	@Modifying
	@Query("UPDATE Solution s " +
		"SET s.deletedAt = CURRENT_TIMESTAMP " +
		"WHERE s.problem IN ("
		+ "SELECT p "
		+ "FROM Problem p "
		+ "WHERE p.studyGroup = :studyGroup)")
	void deleteAllByStudyGroup(StudyGroup studyGroup);

	@Modifying
	@Query("UPDATE Solution s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.problem = :problem")
	void deleteAllByProblem(Problem problem);

	@Modifying
	@Query("UPDATE Solution s "
		+ "SET s.deletedAt = CURRENT_TIMESTAMP "
		+ "WHERE s.user = :user "
		+ "AND s.problem IN ("
		+ "SELECT p "
		+ "FROM Problem p "
		+ "WHERE p.studyGroup = :studyGroup)")
	void deleteAllByStudyGroupAndUser(StudyGroup studyGroup, User user);

	@Query("SELECT s "
		+ "FROM Solution s "
		+ "WHERE s.deletedAt IS NULL "
		+ "AND s.user = :user "
		+ "AND s.problem = :problem")
	List<Solution> findAllByUserAndProblem(User user, Problem problem);
}
