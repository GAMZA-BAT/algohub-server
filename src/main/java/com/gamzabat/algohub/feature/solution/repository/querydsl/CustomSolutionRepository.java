package com.gamzabat.algohub.feature.solution.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.enums.ProgressCategory;
import com.gamzabat.algohub.feature.user.domain.User;

public interface CustomSolutionRepository {
	Page<Solution> findAllFilteredSolutions(
		Problem problem,
		String nickname,
		String language,
		String result,
		Pageable pageable);

	Page<Solution> findAllFilteredMySolutionsInGroup(
		User user,
		StudyGroup group,
		Integer problemNumber,
		String language,
		String result,
		ProgressCategory category,
		Pageable pageable);

	Page<Solution> findAllFilteredMySolutions(
		User user,
		Integer problemNumber,
		String language,
		String result,
		ProgressCategory category,
		Pageable pageable);

	boolean existsByUserAndProblemAndResult(User user, Problem problem, String result);
}
