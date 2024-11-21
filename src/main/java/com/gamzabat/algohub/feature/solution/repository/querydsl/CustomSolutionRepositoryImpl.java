package com.gamzabat.algohub.feature.solution.repository.querydsl;

import static com.gamzabat.algohub.constants.BOJResultConstants.*;
import static com.gamzabat.algohub.feature.group.studygroup.domain.QStudyGroup.*;
import static com.gamzabat.algohub.feature.problem.domain.QProblem.*;
import static com.gamzabat.algohub.feature.solution.domain.QSolution.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.constants.LanguageConstants;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.user.domain.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomSolutionRepositoryImpl implements CustomSolutionRepository {
	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsByUserAndProblemAndResult(User user, Problem problem, String result) {
		JPAQuery<Solution> query = queryFactory.selectFrom(solution)
			.where(solution.user.eq(user))
			.where(solution.problem.eq(problem));

		addResultFilter(result, query);

		return query.fetchFirst() != null;
	}

	@Override
	public Page<Solution> findAllFilteredSolutions(Problem problem, String nickname, String language, String result,
		Pageable pageable) {
		JPAQuery<Solution> query = queryFactory.selectFrom(solution)
			.where(solution.problem.eq(problem));

		addNicknameFilter(nickname, query);
		addLanguageFilter(language, query);
		addResultFilter(result, query);

		query.orderBy(solution.solvedDateTime.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		JPAQuery<Long> countQuery = solutionCountQuery(query);

		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	@Override
	public Page<Solution> findAllFilteredMySolutionsInGroup(User user, StudyGroup group, Integer problemNumber,
		String language,
		String result, Pageable pageable) {
		JPAQuery<Solution> query = queryFactory.selectFrom(solution)
			.join(solution.problem, problem).fetchJoin()
			.join(problem.studyGroup, studyGroup).fetchJoin()
			.where(solution.problem.studyGroup.eq(group)
				.and(solution.user.eq(user)));

		addProblemFilter(problemNumber, query);
		addLanguageFilter(language, query);
		addResultFilter(result, query);

		JPAQuery<Long> countQuery = solutionCountQuery(query);
		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	@Override
	public Page<Solution> findAllFilteredMySolutions(User user, Integer problemNumber,
		String language,
		String result, Pageable pageable) {
		JPAQuery<Solution> query = queryFactory.selectFrom(solution)
			.where(solution.user.eq(user));

		addProblemFilter(problemNumber, query);
		addLanguageFilter(language, query);
		addResultFilter(result, query);

		JPAQuery<Long> countQuery = solutionCountQuery(query);
		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	private void addResultFilter(String result, JPAQuery<Solution> query) {
		if (result != null && !result.isBlank()) {
			if (result.equals(CORRECT))
				query.where(solution.result.eq(result)
					.or(solution.result.endsWith("점")));
			else if (result.equals(WRONG_OUTPUT_FORMAT_CUSTOM))
				query.where(solution.result.eq(WRONG_OUTPUT_FORMAT));
			else
				query.where(solution.result.startsWith(result));
		}
	}

	private void addNicknameFilter(String nickname, JPAQuery<Solution> query) {
		if (nickname != null && !nickname.isBlank())
			query.where(solution.user.nickname.eq(nickname));
	}

	private void addLanguageFilter(String language, JPAQuery<Solution> query) {
		if (language != null && !language.isBlank())
			languageFilter(query, language);
	}

	private void addProblemFilter(Integer problemNumber, JPAQuery<Solution> query) {
		if (problemNumber != null)
			query.where(solution.problem.number.eq(problemNumber));
	}

	private void languageFilter(JPAQuery<Solution> query, String language) {
		switch (language) {
			case "C":
				query.where(solution.language.in(LanguageConstants.C_BOUNDARY));
				break;
			case "C++":
				query.where(solution.language.in(LanguageConstants.CPP_BOUNDARY));
				break;
			case "Java":
				query.where(solution.language.in(LanguageConstants.JAVA_BOUNDARY));
				break;
			case "Python":
				query.where(solution.language.in(LanguageConstants.PYTHON_BOUNDARY));
				break;
			case "Rust":
				query.where(solution.language.in(LanguageConstants.RUST_BOUNDARY));
				break;
		}

	}

	private JPAQuery<Long> solutionCountQuery(JPAQuery<Solution> query) {
		return queryFactory.select(solution.count())
			.from(solution)
			.where(query.getMetadata().getWhere());
	}
}
