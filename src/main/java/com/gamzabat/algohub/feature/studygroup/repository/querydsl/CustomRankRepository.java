package com.gamzabat.algohub.feature.studygroup.repository.querydsl;

import java.util.List;

import com.gamzabat.algohub.feature.studygroup.domain.Rank;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;

public interface CustomRankRepository {
	List<Rank> findAllByStudyGroup(StudyGroup studyGroup);
}
