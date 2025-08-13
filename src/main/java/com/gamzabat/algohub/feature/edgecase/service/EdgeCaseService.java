package com.gamzabat.algohub.feature.edgecase.service;

import static com.gamzabat.algohub.constants.ApiConstants.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;
import com.gamzabat.algohub.feature.edgecase.dto.CreateEdgeCaseRequest;
import com.gamzabat.algohub.feature.edgecase.repository.EdgeCaseRepository;
import com.gamzabat.algohub.feature.problem.exception.NotBojLinkException;
import com.gamzabat.algohub.feature.problem.service.ProblemService;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdgeCaseService {
	private final EdgeCaseRepository edgeCaseRepository;
	private final ProblemService problemService;

	@Transactional
	public void createEdgeCase(User user, CreateEdgeCaseRequest request) {
		User author = user;
		String link = request.link();

		String number = getProblemId(link);
		JsonNode apiResult = problemService.fetchProblemDetails(number);
		int level = problemService.getProblemLevel(apiResult);
		String title = problemService.getProblemTitle(apiResult);

		EdgeCase edgeCase = EdgeCase.builder().input(request.input()).level(level).title(title).link(
			link).output(request.output()).number(Integer.parseInt(number)).author(author).like(0).build();

		edgeCaseRepository.save(edgeCase);
	}

	private String getProblemId(String url) {
		String[] parts = url.split("/");
		if (parts.length < 3 || !parts[2].equals(BOJ_PROBLEM_URL))
			throw new NotBojLinkException(HttpStatus.BAD_REQUEST.value(), "백준 링크가 아닙니다");
		return parts[parts.length - 1];
	}
}
