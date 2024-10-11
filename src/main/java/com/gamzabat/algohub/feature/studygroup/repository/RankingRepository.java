package com.gamzabat.algohub.feature.studygroup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.Ranking;
import com.gamzabat.algohub.feature.studygroup.repository.querydsl.CustomRankingRepository;

public interface RankingRepository extends JpaRepository<Ranking, Long>, CustomRankingRepository {
	Optional<Ranking> findByMember(GroupMember member);

	void deleteByMember(GroupMember member);
}
