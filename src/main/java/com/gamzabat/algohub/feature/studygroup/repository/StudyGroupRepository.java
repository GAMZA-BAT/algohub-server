package com.gamzabat.algohub.feature.studygroup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.repository.querydsl.CustomStudyGroupRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long>, CustomStudyGroupRepository {
	Optional<StudyGroup> findByGroupCode(String groupCode);

	Optional<StudyGroup> findById(Long id);
}
