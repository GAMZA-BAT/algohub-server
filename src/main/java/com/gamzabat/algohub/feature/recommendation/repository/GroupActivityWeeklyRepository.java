package com.gamzabat.algohub.feature.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.recommendation.domain.GroupActivityWeekly;

public interface GroupActivityWeeklyRepository extends JpaRepository<GroupActivityWeekly, Long> {
}