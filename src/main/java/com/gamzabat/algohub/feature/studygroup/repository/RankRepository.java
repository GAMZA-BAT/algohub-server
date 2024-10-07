package com.gamzabat.algohub.feature.studygroup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.Rank;
import com.gamzabat.algohub.feature.studygroup.repository.querydsl.CustomRankRepository;

public interface RankRepository extends JpaRepository<Rank, Long>, CustomRankRepository {
	Optional<Rank> findByMember(GroupMember member);
}
