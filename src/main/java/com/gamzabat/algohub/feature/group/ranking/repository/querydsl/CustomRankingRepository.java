package com.gamzabat.algohub.feature.group.ranking.repository.querydsl;

import java.util.List;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

public interface CustomRankingRepository {
	List<Ranking> findAllByStudyGroup(StudyGroup studyGroup);
}
