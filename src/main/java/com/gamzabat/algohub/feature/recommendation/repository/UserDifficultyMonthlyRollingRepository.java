package com.gamzabat.algohub.feature.recommendation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.recommendation.domain.UserDifficultyMonthlyRolling;

public interface UserDifficultyMonthlyRollingRepository extends JpaRepository<UserDifficultyMonthlyRolling, Long> {
	Optional<UserDifficultyMonthlyRolling> findTop1ByUser_IdOrderByWindowEndDesc(Long userId);
}
