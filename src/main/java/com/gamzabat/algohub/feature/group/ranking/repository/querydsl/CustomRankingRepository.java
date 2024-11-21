package com.gamzabat.algohub.feature.group.ranking.repository.querydsl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

public interface CustomRankingRepository {
	Page<Ranking> findAllByStudyGroup(StudyGroup studyGroup, Pageable pageable);

	List<Ranking> findAllByStudyGroup(StudyGroup studyGroup);

	void deleteAllByStudyGroup(StudyGroup studyGroup);
}
