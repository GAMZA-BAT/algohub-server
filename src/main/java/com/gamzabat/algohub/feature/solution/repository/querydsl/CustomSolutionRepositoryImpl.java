package com.gamzabat.algohub.feature.solution.repository.querydsl;

import static com.gamzabat.algohub.feature.solution.domain.QSolution.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.constants.LanguageConstants;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class CustomSolutionRepositoryImpl implements CustomSolutionRepository {
	private final JPAQueryFactory queryFactory;

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

	private void addResultFilter(String result, JPAQuery<Solution> query) {
		if (result != null && !result.isBlank()) {
			if (result.equals("맞았습니다!!"))
				query.where(solution.result.eq(result)
					.or(solution.result.endsWith("점")));
			else if (result.equals("런타임 에러"))
				query.where(solution.result.startsWith("런타임 에러"));
			else
				query.where(solution.result.eq(result));
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

	private void languageFilter(JPAQuery<Solution> query, String language) {
		switch (language) {
			case "C":
				query.where(solution.language.in(LanguageConstants.C_BOUNDARY));
			case "C++":
				query.where(solution.language.in(LanguageConstants.CPP_BOUNDARY));
			case "Java":
				query.where(solution.language.in(LanguageConstants.JAVA_BOUNDARY));
			case "Python":
				query.where(solution.language.in(LanguageConstants.RUST_BOUNDARY));
		}

	}

	private JPAQuery<Long> solutionCountQuery(JPAQuery<Solution> query) {
		return queryFactory.select(solution.count())
			.from(solution)
			.where(query.getMetadata().getWhere());
	}
}
