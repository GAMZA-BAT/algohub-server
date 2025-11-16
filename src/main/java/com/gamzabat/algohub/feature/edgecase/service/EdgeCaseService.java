package com.gamzabat.algohub.feature.edgecase.service;

import static com.gamzabat.algohub.constants.ApiConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCaseLike;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCaseSortType;
import com.gamzabat.algohub.feature.edgecase.dto.CreateEdgeCaseRequest;
import com.gamzabat.algohub.feature.edgecase.dto.GetEdgeCaseListResponse;
import com.gamzabat.algohub.feature.edgecase.dto.GetEdgeCaseResponse;
import com.gamzabat.algohub.feature.edgecase.dto.TogleEdgeCaseResponse;
import com.gamzabat.algohub.feature.edgecase.exception.CannotFoundEdgeCaseException;
import com.gamzabat.algohub.feature.edgecase.exception.NotAuthorizedUserException;
import com.gamzabat.algohub.feature.edgecase.repository.EdgeCaseLikeRepository;
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
	private final EdgeCaseLikeRepository edgeCaseLikeRepository;

	public void createEdgeCase(User user, CreateEdgeCaseRequest request) {
		User author = user;
		String link = request.link();

		String number = getProblemId(link);
		JsonNode apiResult = problemService.fetchProblemDetails(number);
		int level = problemService.getProblemLevel(apiResult);
		String title = problemService.getProblemTitle(apiResult);

		saveEdgeCase(author, request, level, title, Integer.parseInt(number));
	}

	private void saveEdgeCase(User author, CreateEdgeCaseRequest request, int level, String title, int number) {
		EdgeCase edgeCase = EdgeCase.builder().input(request.input()).level(level).title(title).link(
			request.link()).output(request.output()).problemNumber(number).author(author).build();

		edgeCaseRepository.save(edgeCase);
	}

	public GetEdgeCaseListResponse getEdgeCaseList(User user, Integer problemNumber, EdgeCaseSortType sort) {
		List<EdgeCase> edgeCaseList;
		Set<Long> likedEdgeCaseIds = Collections.emptySet();

		if (problemNumber == null) {
			switch (sort) {
				case LIKE:
					edgeCaseList = edgeCaseRepository.findAllByOrderByLikeCountDesc();
					break;
				case OLD:
					edgeCaseList = edgeCaseRepository.findAllByOrderByCreatedAtAsc();
					break;
				case RECENT:
				default:
					edgeCaseList = edgeCaseRepository.findAllByOrderByCreatedAtDesc();
					break;
			}
		} else {
			switch (sort) {
				case LIKE:
					edgeCaseList = edgeCaseRepository.findAllByProblemNumberOrderByLikeCountDesc(problemNumber);
					break;
				case OLD:
					edgeCaseList = edgeCaseRepository.findAllByProblemNumberOrderByCreatedAtAsc(problemNumber);
					break;
				case RECENT:
				default:
					edgeCaseList = edgeCaseRepository.findAllByProblemNumberOrderByCreatedAtDesc(problemNumber);
					break;
			}
		}

		if (user != null && !edgeCaseList.isEmpty()) {
			List<EdgeCaseLike> myLikes =
				edgeCaseLikeRepository.findByUserAndEdgeCaseIn(user, edgeCaseList);

			likedEdgeCaseIds = myLikes.stream()
				.map(like -> like.getEdgeCase().getId())
				.collect(Collectors.toSet());
		}

		final Set<Long> likedIds = likedEdgeCaseIds;

		List<GetEdgeCaseResponse> responseList = edgeCaseList.stream()
			.map(edgeCase -> new GetEdgeCaseResponse(
				edgeCase.getId().intValue(),
				edgeCase.getLevel(),
				edgeCase.getProblemNumber(),
				edgeCase.getTitle(),
				edgeCase.getInput(),
				edgeCase.getOutput(),
				edgeCase.getLikeCount(),
				likedIds.contains(edgeCase.getId())
			))
			.collect(Collectors.toList());

		return new GetEdgeCaseListResponse(responseList);
	}

	@Transactional
	public void deleteEdgeCase(User user, Long edgeCaseId) {
		EdgeCase edgeCase = edgeCaseRepository.findById(edgeCaseId)
			.orElseThrow(() -> new CannotFoundEdgeCaseException("존재하지 않는 반례입니다.", HttpStatus.NOT_FOUND));

		User author = edgeCase.getAuthor();

		if (!user.getId().equals(author.getId()))
			throw new NotAuthorizedUserException("반례를 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
		List<EdgeCaseLike> likes = edgeCaseLikeRepository.findAllByEdgeCase(edgeCase);
		edgeCaseLikeRepository.deleteAll(likes);
		edgeCaseRepository.delete(edgeCase);
	}

	@Transactional
	public TogleEdgeCaseResponse togleEdgeCaseLike(User user, Long edgeCaseId) {
		EdgeCase edgeCase = edgeCaseRepository.findById(edgeCaseId)
			.orElseThrow(() -> new CannotFoundEdgeCaseException("존재하지 않는 반례입니다.", HttpStatus.NOT_FOUND));

		EdgeCaseLike edgeCaseLike = edgeCaseLikeRepository.findByEdgeCaseAndUser(edgeCase, user).orElse(null);
		Boolean isLike = false;
		if (edgeCaseLike == null) {
			edgeCaseLike = EdgeCaseLike.builder().user(user).edgeCase(edgeCase).build();
			edgeCaseLikeRepository.save(edgeCaseLike);
			edgeCase.increaseLikeCount();
			isLike = true;
		} else {
			edgeCaseLikeRepository.delete(edgeCaseLike);
			edgeCase.decreaseLikeCount();
		}

		return new TogleEdgeCaseResponse(isLike);
	}

	private String getProblemId(String url) {
		String[] parts = url.split("/");
		if (parts.length < 3 || !parts[2].equals(BOJ_URL))
			throw new NotBojLinkException(HttpStatus.BAD_REQUEST.value(), "백준 링크가 아닙니다");
		return parts[parts.length - 1];
	}

}
