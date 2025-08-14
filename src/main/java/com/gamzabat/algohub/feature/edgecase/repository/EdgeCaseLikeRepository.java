package com.gamzabat.algohub.feature.edgecase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCaseLike;
import com.gamzabat.algohub.feature.user.domain.User;

public interface EdgeCaseLikeRepository extends JpaRepository<EdgeCaseLike, Long> {
	boolean existsByEdgeCaseAndUser(EdgeCase edgeCase, User user);
}
