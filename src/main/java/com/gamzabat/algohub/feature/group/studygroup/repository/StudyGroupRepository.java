package com.gamzabat.algohub.feature.group.studygroup.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.repository.querydsl.CustomStudyGroupRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long>, CustomStudyGroupRepository {
	@Query("select sg from StudyGroup sg where sg.groupCode = :groupCode and sg.deletedAt is null")
	Optional<StudyGroup> findByGroupCode(String groupCode);

	@Query("select sg from StudyGroup sg where sg.id = :id and sg.deletedAt is null")
	Optional<StudyGroup> findById(Long id);

	@Query("select sg from StudyGroup sg where lower(sg.name) like(concat('%', lower(:searchPattern),'%'))"
		+ "or lower(sg.introduction) like(concat('%',lower(:searchPattern),'%'))")
	Page<StudyGroup> findBySearchPattern(@Param("searchPattern") String searchPattern, Pageable pageable);
}
