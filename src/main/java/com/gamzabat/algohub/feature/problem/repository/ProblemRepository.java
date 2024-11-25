package com.gamzabat.algohub.feature.problem.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
	Page<Problem> findAllByStudyGroup(StudyGroup studyGroup, Pageable pageable);

	List<Problem> findAllByNumber(Integer Number);

	List<Problem> findAllByStudyGroupAndEndDateBetween(StudyGroup studyGroup, LocalDate now, LocalDate tomorrow);

	Page<Problem> findAllByStudyGroupAndStartDateAfter(StudyGroup studyGroup, LocalDate startDate, Pageable pageable);

	List<Problem> findAllByStartDate(LocalDate startDate);

	@Query("SELECT COUNT(p) FROM Problem p WHERE p.studyGroup.id = :groupId")
	Long countProblemsByGroupId(@Param("groupId") Long groupId);

	List<Problem> findAllByEndDate(LocalDate endDate);

	@Modifying
	@Query("delete from Problem p where p.studyGroup = :studyGroup")
	void deleteAllByStudyGroup(StudyGroup studyGroup);

	Page<Problem> findAllByStudyGroupAndEndDateGreaterThanEqual(StudyGroup studyGroup, LocalDate now,
		Pageable pageable);

	Page<Problem> findAllByStudyGroupAndEndDateBefore(StudyGroup studyGroup, LocalDate now, Pageable pageable);
}
