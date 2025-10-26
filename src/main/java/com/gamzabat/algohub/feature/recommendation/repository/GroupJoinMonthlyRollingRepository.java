package com.gamzabat.algohub.feature.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.recommendation.domain.GroupJoinMonthlyRolling;

public interface GroupJoinMonthlyRollingRepository extends JpaRepository<GroupJoinMonthlyRolling, Long> {
}
