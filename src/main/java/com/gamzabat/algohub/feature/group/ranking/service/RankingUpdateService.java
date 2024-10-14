package com.gamzabat.algohub.feature.group.ranking.service;

import java.util.List;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class RankingUpdateService {
	private final RankingRepository rankingRepository;

	@Autowired
	public RankingUpdateService(RankingRepository rankingRepository) {
		this.rankingRepository = rankingRepository;
	}

	@AfterReturning(
		value = "execution(* com.gamzabat.algohub.feature.group.studygroup.service.StudyGroupService.deleteMemberFromStudyGroup(..)) && args(.., studyGroup)"
	)
	@Transactional
	public void updateRanking(StudyGroup studyGroup) {
		List<Ranking> rankings = rankingRepository.findAllByStudyGroup(studyGroup);
		rankings.sort((r1, r2) -> {
			return r2.getSolvedCount() - r1.getSolvedCount(); // TODO : 2차로 score 비교 추가
		});
		for (Ranking r : rankings) {
			int originRank = r.getCurrentRank();
			int newRank = rankings.indexOf(r) + 1;
			r.updateRank(newRank);
			r.updateRankDiff(generateRankDiffString(originRank, newRank));
		}
		log.info("success to update ranking");
	}

	private String generateRankDiffString(int originRank, int newRank) {
		int rankDiff = originRank - newRank;
		if (rankDiff > 0)
			return "+" + rankDiff;
		else if (rankDiff < 0)
			return String.valueOf(rankDiff);
		else
			return "-";
	}
}
