package com.gamzabat.algohub.feature.problem.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.querydsl.CustomProblemRepository;
import com.gamzabat.algohub.feature.user.domain.User;

public interface ProblemRepository extends JpaRepository<Problem, Long>, CustomProblemRepository {
	@Query("select p from Problem p where p.id = :id and p.deletedAt is null")
	Optional<Problem> findById(Long id);

	Page<Problem> findAllByStudyGroup(StudyGroup studyGroup, Pageable pageable);

	@Query("""
		    SELECT p FROM Problem p
			JOIN FETCH p.studyGroup
		    WHERE p.number = :number
		  	AND p.endDate > :today
			AND p.deletedAt is null
		  	AND p.studyGroup.deletedAt is null
		      AND EXISTS (
		        SELECT gm FROM GroupMember gm
		        WHERE gm.user = :user
		          AND gm.studyGroup = p.studyGroup
		    )
		""")
	List<Problem> findValidProblemsByNumberAndUser(Integer number, LocalDate today, User user);

	@Query("select p from Problem p "
		+ "where p.deletedAt is null "
		+ "and p.studyGroup = :studyGroup "
		+ "and p.endDate between :start and :end")
	List<Problem> findAllByStudyGroupAndEndDateBetween(StudyGroup studyGroup, LocalDate start, LocalDate end);

	@Query("select p from Problem p "
		+ "where p.deletedAt is null "
		+ "and p.studyGroup.deletedAt is null "
		+ "and p.startDate = :startDate")
	List<Problem> findAllByStartDate(LocalDate startDate);

	@Query("SELECT COUNT(p) FROM Problem p "
		+ "WHERE p.deletedAt IS NULL "
		+ "AND p.studyGroup = :group")
	Long countProblemsByGroup(StudyGroup group);

	@Query("select p from Problem p "
		+ "where p.deletedAt is null "
		+ "and p.studyGroup.deletedAt is null "
		+ "and p.endDate = :endDate")
	List<Problem> findAllByEndDate(LocalDate endDate);

	@Modifying
	@Query("update Problem p set p.deletedAt = CURRENT_TIMESTAMP where p.studyGroup = :studyGroup")
	void deleteAllByStudyGroup(StudyGroup studyGroup);
}
