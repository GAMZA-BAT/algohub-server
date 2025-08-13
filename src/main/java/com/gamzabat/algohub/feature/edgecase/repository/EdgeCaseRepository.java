package com.gamzabat.algohub.feature.edgecase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;

public interface EdgeCaseRepository extends JpaRepository<EdgeCase, Long> {

}
