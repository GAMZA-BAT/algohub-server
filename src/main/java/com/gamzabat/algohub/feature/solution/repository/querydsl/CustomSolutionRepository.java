package com.gamzabat.algohub.feature.solution.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;

public interface CustomSolutionRepository {
	Page<Solution> findAllFilteredSolutions(
		Problem problem,
		String nickname,
		String language,
		String result,
		Pageable pageable);
}
