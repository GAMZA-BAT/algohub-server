package com.gamzabat.algohub.feature.edgecase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCaseLike;
import com.gamzabat.algohub.feature.user.domain.User;

public interface EdgeCaseLikeRepository extends JpaRepository<EdgeCaseLike, Long> {
	Optional<EdgeCaseLike> findByEdgeCaseAndUser(EdgeCase edgeCase, User user);
	List<EdgeCaseLike> findAllByEdgeCase(EdgeCase edgeCase);
	List<EdgeCaseLike> findByUserAndEdgeCaseIn(User user, List<EdgeCase> edgeCases);
}
