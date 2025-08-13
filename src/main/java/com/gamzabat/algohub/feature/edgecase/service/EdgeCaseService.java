package com.gamzabat.algohub.feature.edgecase.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.edgecase.controller.EdgeCaseController;
import com.gamzabat.algohub.feature.edgecase.domain.EdgeCase;
import com.gamzabat.algohub.feature.edgecase.dto.CreateEdgeCaseRequest;
import com.gamzabat.algohub.feature.edgecase.repository.EdgeCaseRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdgeCaseService {
	private final EdgeCaseRepository edgeCaseRepository;

	@Transactional
	public void createEdgeCase(User user, CreateEdgeCaseRequest request) {
		EdgeCase edgeCase = EdgeCase.builder().input(request.input()).level(request.level()).title(request.tile()).link(
			request.link()).output(request.output()).number(request.number()).author(user).like(0).build();

		edgeCaseRepository.save(edgeCase);
	}


}
