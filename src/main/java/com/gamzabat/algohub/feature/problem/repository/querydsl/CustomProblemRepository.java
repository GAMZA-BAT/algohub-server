package com.gamzabat.algohub.feature.problem.repository.querydsl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;

public interface CustomProblemRepository {
	Page<Problem> findAllInProgressProblem(User user, StudyGroup group, Boolean unsolvedOnly, Pageable pageable);

	Page<Problem> findAllExpiredProblem(StudyGroup group, Pageable pageable);

	Page<Problem> findAllQueuedProblem(StudyGroup group, Pageable pageable);
}
