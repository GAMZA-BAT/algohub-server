package com.gamzabat.algohub.feature.problem.repository.querydsl;

import static com.gamzabat.algohub.feature.problem.domain.QProblem.*;
import static com.gamzabat.algohub.feature.solution.domain.QSolution.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomProblemRepositoryImpl implements CustomProblemRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Problem> findAllInProgressProblem(User user, StudyGroup group, Boolean unsolvedOnly,
		Pageable pageable) {
		BooleanExpression condition = problem.studyGroup.eq(group)
			.and(problem.deletedAt.isNull())
			.and(problem.startDate.loe(LocalDate.now()))
			.and(problem.endDate.goe(LocalDate.now()));

		if (unsolvedOnly) {
			condition = addUnsolvedProblemFilter(condition, user);
		}

		return findAllProblemByCondition(condition, pageable);
	}

	@Override
	public List<Problem> findAllInProgressProblem(StudyGroup group) {
		JPAQuery<Problem> query = queryFactory.selectFrom(problem)
			.where(problem.studyGroup.eq(group)
				.and(problem.deletedAt.isNull())
				.and(problem.startDate.loe(LocalDate.now()))
				.and(problem.endDate.goe(LocalDate.now())));
		return query.fetch();
	}

	@Override
	public Page<Problem> findAllExpiredProblem(StudyGroup group, Pageable pageable) {
		BooleanExpression condition = problem.studyGroup.eq(group)
			.and(problem.deletedAt.isNull())
			.and(problem.endDate.before(LocalDate.now()));

		return findAllProblemByCondition(condition, pageable);
	}

	@Override
	public Page<Problem> findAllQueuedProblem(StudyGroup group, Pageable pageable) {
		BooleanExpression condition = problem.studyGroup.eq(group)
			.and(problem.deletedAt.isNull())
			.and(problem.startDate.after(LocalDate.now()));

		return findAllProblemByCondition(condition, pageable);
	}

	private Page<Problem> findAllProblemByCondition(BooleanExpression condition, Pageable pageable) {
		JPAQuery<Problem> query = getSelectQuery(pageable, condition);

		JPAQuery<Long> countQuery = queryFactory.select(problem.count())
			.from(problem)
			.where(condition);

		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	private JPAQuery<Problem> getSelectQuery(Pageable pageable, BooleanExpression condition) {
		return queryFactory.selectFrom(problem)
			.where(condition)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());
	}

	private BooleanExpression addUnsolvedProblemFilter(BooleanExpression condition, User user) {
		return condition
			.and(
				JPAExpressions.selectFrom(solution)
					.where(solution.user.eq(user)
						.and(solution.problem.eq(problem))
						.and(solution.result.eq(BOJResultConstants.CORRECT)
							.or(solution.result.like("%점"))))
					.notExists()
			);
	}
}
