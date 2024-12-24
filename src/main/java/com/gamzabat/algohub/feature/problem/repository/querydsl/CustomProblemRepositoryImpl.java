package com.gamzabat.algohub.feature.problem.repository.querydsl;

import static com.gamzabat.algohub.feature.problem.domain.QProblem.*;
import static com.gamzabat.algohub.feature.solution.domain.QSolution.*;

import java.time.LocalDate;

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
		JPAQuery<Problem> query = queryFactory.selectFrom(problem)
			.where(problem.studyGroup.eq(group)
				.and(problem.deletedAt.isNull())
				.and(problem.startDate.loe(LocalDate.now()))
				.and(problem.endDate.goe(LocalDate.now())));

		if (unsolvedOnly) {
			addUnsolvedProblemFilter(query, user);
		}

		query.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		JPAQuery<Long> countQuery = problemCountQuery(user, group, unsolvedOnly);

		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	@Override
	public Page<Problem> findAllExpiredProblem(StudyGroup group, Pageable pageable) {
		BooleanExpression condition = problem.studyGroup.eq(group)
			.and(problem.deletedAt.isNull())
			.and(problem.endDate.before(LocalDate.now()));

		JPAQuery<Problem> query = getSelectQuery(pageable,
			condition);

		JPAQuery<Long> countQuery = queryFactory.select(problem.count())
			.from(problem)
			.where(condition);

		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	@Override
	public Page<Problem> findAllQueuedProblem(StudyGroup group, Pageable pageable) {
		BooleanExpression condition = problem.studyGroup.eq(group)
			.and(problem.deletedAt.isNull())
			.and(problem.startDate.after(LocalDate.now()));

		JPAQuery<Problem> query = getSelectQuery(pageable,
			condition);

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

	private void addUnsolvedProblemFilter(JPAQuery<?> query, User user) {
		query
			.where(
				JPAExpressions.selectFrom(solution)
					.where(solution.user.eq(user)
						.and(solution.problem.eq(problem))
						.and(solution.result.eq(BOJResultConstants.CORRECT)
							.or(solution.result.like("%점"))))
					.notExists()
			);
	}

	private JPAQuery<Long> problemCountQuery(User user, StudyGroup group, boolean unsolvedOnly) {
		JPAQuery<Long> query = queryFactory.select(problem.count())
			.from(problem)
			.where(problem.studyGroup.eq(group)
				.and(problem.startDate.loe(LocalDate.now()))
				.and(problem.endDate.goe(LocalDate.now())));

		if (unsolvedOnly) {
			addUnsolvedProblemFilter(query, user);
		}
		return query;
	}
}
