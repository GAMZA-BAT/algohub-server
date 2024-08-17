package com.gamzabat.algohub.feature.problem.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
	Page<Problem> findAllByStudyGroup(StudyGroup studyGroup, Pageable pageable);
	Problem getById(Long id);
	List<Problem> findAllByNumber(Integer Number);
	List<Problem> findAllByStudyGroupAndEndDateBetween(StudyGroup studyGroup, LocalDate now,LocalDate tomorrow);
	@Query("SELECT COUNT(p) FROM Problem p WHERE p.studyGroup.id = :groupId")
	Long countProblemsByGroupId(@Param("groupId") Long groupId);
}
