package com.gamzabat.algohub.feature.edgecase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;

public interface EdgeCaseRepository extends JpaRepository<EdgeCase, Long> {
	// RECENT
	List<EdgeCase> findAllByProblemNumberOrderByCreatedAtDesc(Integer problemNumber);

	List<EdgeCase> findAllByOrderByCreatedAtDesc();

	// LIKE
	List<EdgeCase> findAllByProblemNumberOrderByLikeCountDesc(Integer problemNumber);

	List<EdgeCase> findAllByOrderByLikeCountDesc();

	// OLD
	List<EdgeCase> findAllByProblemNumberOrderByCreatedAtAsc(Integer problemNumber);

	List<EdgeCase> findAllByOrderByCreatedAtAsc();
}
