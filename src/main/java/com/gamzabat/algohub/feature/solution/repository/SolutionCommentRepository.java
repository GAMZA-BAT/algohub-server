package com.gamzabat.algohub.feature.solution.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.solution.domain.SolutionComment;

public interface SolutionCommentRepository extends JpaRepository<SolutionComment, Long> {
	List<SolutionComment> findAllBySolution(Solution solution);

	@Query("SELECT COUNT(c) FROM SolutionComment c WHERE c.solution.id = :solutionId")
	long countCommentsBySolutionId(@Param("solutionId") Long solutionId);

	@Modifying
	@Query("DELETE FROM SolutionComment sc WHERE sc.solution.problem.studyGroup = :studyGroup")
	void deleteAllByStudyGroup(StudyGroup studyGroup);

	@Query("SELECT COUNT(sc) FROM SolutionComment sc " +
		"JOIN sc.solution s " +
		"WHERE s.problem.studyGroup = :studyGroup AND sc.createdAt BETWEEN :start AND :end")
	Long countByStudyGroupAndCreatedAtBetween(@Param("studyGroup") StudyGroup studyGroup,
		@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
	
}
