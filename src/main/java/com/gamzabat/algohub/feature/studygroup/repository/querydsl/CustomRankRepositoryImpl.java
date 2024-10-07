package com.gamzabat.algohub.feature.studygroup.repository.querydsl;

import static com.gamzabat.algohub.feature.studygroup.domain.QRank.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.feature.studygroup.domain.Rank;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomRankRepositoryImpl implements CustomRankRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<Rank> findAllByStudyGroup(StudyGroup studyGroup) {
		return queryFactory.selectFrom(rank)
			.where(rank.member.studyGroup.eq(studyGroup))
			.fetch();
	}
}
