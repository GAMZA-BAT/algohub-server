package com.gamzabat.algohub.feature.studygroup.repository.querydsl;

import java.util.List;

import com.gamzabat.algohub.feature.studygroup.domain.Ranking;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;

public interface CustomRankingRepository {
	List<Ranking> findAllByStudyGroup(StudyGroup studyGroup);
}
