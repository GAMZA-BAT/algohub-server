package com.gamzabat.algohub.feature.studygroup.repository.querydsl;

import java.util.List;

import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;

public interface CustomStudyGroupRepository {
	List<StudyGroup> findAllByUser(User user);

}
