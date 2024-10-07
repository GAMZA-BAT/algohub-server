package com.gamzabat.algohub.feature.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.studygroup.domain.Rank;

public interface RankRepository extends JpaRepository<Rank, Long> {
}
