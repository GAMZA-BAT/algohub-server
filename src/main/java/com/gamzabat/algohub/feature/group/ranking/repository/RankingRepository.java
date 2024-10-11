package com.gamzabat.algohub.feature.group.ranking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.repository.querydsl.CustomRankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;

public interface RankingRepository extends JpaRepository<Ranking, Long>, CustomRankingRepository {
	Optional<Ranking> findByMember(GroupMember member);

	void deleteByMember(GroupMember member);
}
