package com.gamzabat.algohub.feature.solution.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.user.domain.User;

public interface SolutionRepository extends JpaRepository<Solution, Long>, JpaSpecificationExecutor<Solution> {
	Page<Solution> findAllByProblemOrderBySolvedDateTimeDesc(Problem problem, Pageable pageable);

	default Page<Solution> findSolutions(Problem problem, String nickname, String language, List<String> results,
		Pageable pageable) {
		return findAll((Specification<Solution>)(root, query, criteriaBuilder) -> {
			var predicates = criteriaBuilder.conjunction();

			if (problem != null) {
				predicates.getExpressions().add(criteriaBuilder.equal(root.get("problem"), problem));
			}

			if (nickname != null && !nickname.isEmpty()) {
				var userJoin = root.join("user");
				predicates.getExpressions().add(criteriaBuilder.equal(userJoin.get("nickname"), nickname));
			}

			if (language != null && !language.isEmpty()) {
				predicates.getExpressions().add(criteriaBuilder.equal(root.get("language"), language));
			}

			if (results != null && !results.isEmpty()) {
				var resultPredicates = criteriaBuilder.disjunction();
				for (String result : results) {
					resultPredicates.getExpressions().add(criteriaBuilder.like(root.get("result"), "%" + result + "%"));
				}
				predicates.getExpressions().add(resultPredicates);
			}

			return predicates;
		}, pageable);
	}

	Boolean existsByUserAndProblem(User user, Problem problem);

	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId")
	Integer countDistinctUsersByProblemId(@Param("problemId") Long problemId);

	@Query("SELECT COUNT(DISTINCT s.user) FROM Solution s WHERE s.problem.id = :problemId AND s.result = '맞았습니다!!'")
	Integer countDistinctUsersWithCorrectSolutionsByProblemId(@Param("problemId") Long problemId);

	@Query("SELECT COUNT(DISTINCT s.problem.id) FROM Solution s " +
		"JOIN s.problem p " +
		"WHERE s.user = :user " +
		"AND p.studyGroup.id = :groupId " +
		"AND s.result = '맞았습니다!!'")
	Long countDistinctCorrectSolutionsByUserAndGroup(@Param("user") User user, @Param("groupId") Long groupId);

	@Query(
		"SELECT new com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse(u.nickname, u.profileImage, 0, COUNT(DISTINCT s.problem.id)) "
			+
			"FROM Solution s " +
			"JOIN s.user u " +
			"JOIN s.problem p " +
			"JOIN p.studyGroup g " +
			"WHERE s.result = '맞았습니다!!' AND g = :group " +
			"GROUP BY u.id, u.nickname, u.profileImage " +
			"ORDER BY COUNT(DISTINCT s.problem.id) DESC, MAX(s.solvedDateTime) ASC")
	List<GetRankingResponse> findTopUsersByGroup(@Param("group") StudyGroup group);

	boolean existsByUserAndProblemAndResult(User user, Problem problem, String result);
}
