package com.gamzabat.algohub.feature.group.ranking.repository.querydsl;

import static com.gamzabat.algohub.feature.group.ranking.domain.QRanking.*;
import static com.gamzabat.algohub.feature.group.studygroup.domain.QGroupMember.*;
import static com.gamzabat.algohub.feature.user.domain.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomRankingRepositoryImpl implements CustomRankingRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Ranking> findAllByStudyGroup(StudyGroup studyGroup) {
		return queryFactory.selectFrom(ranking)
			.join(ranking.member, groupMember).fetchJoin()
			.join(groupMember.user, user).fetchJoin()
			.where(ranking.member.studyGroup.eq(studyGroup))
			.fetch();
	}
}
