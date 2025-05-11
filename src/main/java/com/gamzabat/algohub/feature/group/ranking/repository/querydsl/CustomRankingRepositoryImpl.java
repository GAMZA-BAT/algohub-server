package com.gamzabat.algohub.feature.group.ranking.repository.querydsl;

import static com.gamzabat.algohub.feature.group.ranking.domain.QRanking.*;
import static com.gamzabat.algohub.feature.group.studygroup.domain.QGroupMember.*;
import static com.gamzabat.algohub.feature.user.domain.QUser.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomRankingRepositoryImpl implements CustomRankingRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Ranking> findAllByStudyGroup(StudyGroup studyGroup, Pageable pageable) {
		JPAQuery<Ranking> query = getRankingsQuery(studyGroup)
			.orderBy(ranking.currentRank.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());
		JPAQuery<Long> countQuery = rankingCountQuery(studyGroup);

		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	@Override
	public List<Ranking> findAllByStudyGroup(StudyGroup studyGroup) {
		return getRankingsQuery(studyGroup).fetch();
	}

	private JPAQuery<Ranking> getRankingsQuery(StudyGroup studyGroup) {
		return queryFactory.selectFrom(ranking)
			.join(ranking.member, groupMember).fetchJoin()
			.join(groupMember.user, user).fetchJoin()
			.where(ranking.member.studyGroup.eq(studyGroup)
				.and(ranking.solvedCount.gt(0)));
	}

	private JPAQuery<Long> rankingCountQuery(StudyGroup studyGroup) {
		return queryFactory.select(ranking.count())
			.from(ranking)
			.join(ranking.member, groupMember)
			.join(groupMember.user, user)
			.where(ranking.member.studyGroup.eq(studyGroup)
				.and(ranking.solvedCount.gt(0)));
	}

	@Override
	public void deleteAllByStudyGroup(StudyGroup group) {
		queryFactory.delete(ranking)
			.where(ranking.member.in(
				JPAExpressions.selectFrom(groupMember)
					.where(groupMember.studyGroup.eq(group))
			))
			.execute();
	}
}
