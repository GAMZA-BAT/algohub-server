package com.gamzabat.algohub.feature.group.studygroup.repository.querydsl;

import static com.gamzabat.algohub.feature.studygroup.domain.QGroupMember.*;
import static com.gamzabat.algohub.feature.studygroup.domain.QStudyGroup.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomStudyGroupRepositoryImpl implements CustomStudyGroupRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<StudyGroup> findAllByUser(User user) {
		return queryFactory.select(groupMember.studyGroup)
			.from(groupMember)
			.join(studyGroup)
			.on(studyGroup.eq(groupMember.studyGroup))
			.where(groupMember.user.eq(user))
			.fetch();
	}
}
