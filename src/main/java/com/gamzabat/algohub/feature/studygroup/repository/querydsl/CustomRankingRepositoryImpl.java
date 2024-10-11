package com.gamzabat.algohub.feature.studygroup.repository.querydsl;

import static com.gamzabat.algohub.feature.studygroup.domain.QRanking.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.feature.studygroup.domain.Ranking;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomRankingRepositoryImpl implements CustomRankingRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Ranking> findAllByStudyGroup(StudyGroup studyGroup) {
		return queryFactory.selectFrom(ranking)
			.where(ranking.member.studyGroup.eq(studyGroup))
			.fetch();
	}
}
