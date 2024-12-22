package com.gamzabat.algohub.feature.group.studygroup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.repository.querydsl.CustomStudyGroupRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long>, CustomStudyGroupRepository {
	@Query("select sg from StudyGroup sg where sg.groupCode = :groupCode and sg.deletedAt is null")
	Optional<StudyGroup> findByGroupCode(String groupCode);

	@Query("select sg from StudyGroup sg where sg.id = :id and sg.deletedAt is null")
	Optional<StudyGroup> findById(Long id);
}
