package com.gamzabat.algohub.feature.solution.repository.querydsl;

import static com.gamzabat.algohub.feature.solution.domain.QSolution.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.gamzabat.algohub.common.LanguageConstants;
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

		query = addNicknameFilter(nickname, query);
		query = addLanguageFilter(language, query);
		query = addResultFilter(result, query);

		query.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		JPAQuery<Long> countQuery = solutionCountQuery(query);

		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
	}

	private JPAQuery<Solution> addResultFilter(String result, JPAQuery<Solution> query) {
		if (result != null && !result.isBlank()) {
			if (result.equals("맞았습니다!!"))
				query.where(solution.result.eq(result)
					.or(solution.result.endsWith("점")));
			else
				query.where(solution.result.eq(result));
		}
		return query;
	}

	private JPAQuery<Solution> addNicknameFilter(String nickname, JPAQuery<Solution> query) {
		if (nickname != null && !nickname.isBlank())
			query.where(solution.user.nickname.eq(nickname));
		return query;
	}

	private JPAQuery<Solution> addLanguageFilter(String language, JPAQuery<Solution> query) {
		if (language != null && !language.isBlank())
			query = languageFilter(query, language);
		return query;
	}

	private JPAQuery<Solution> languageFilter(JPAQuery<Solution> query, String language) {
		return switch (language) {
			case "C" -> query.where(solution.language.in(LanguageConstants.C_BOUNDARY));
			case "C++" -> query.where(solution.language.in(LanguageConstants.CPP_BOUNDARY));
			case "Java" -> query.where(solution.language.in(LanguageConstants.JAVA_BOUNDARY));
			case "Python" -> query.where(solution.language.in(LanguageConstants.PYTHON_BOUNDARY));
			case "Rust" -> query.where(solution.language.in(LanguageConstants.RUST_BOUNDARY));
			default -> query;
		};
	}

	private JPAQuery<Long> solutionCountQuery(JPAQuery<Solution> query) {
		return queryFactory.select(solution.count())
			.from(solution)
			.where(query.getMetadata().getWhere());
	}
}
