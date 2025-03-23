package com.gamzabat.algohub.feature.group.studygroup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.user.domain.User;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
	boolean existsByUserAndStudyGroup(User user, StudyGroup studyGroup);

	Optional<GroupMember> findByUserAndStudyGroup(User user, StudyGroup studyGroup);

	boolean existsByUserAndStudyGroupAndIsVisible(User user, StudyGroup studyGroup, Boolean isVisible);

	List<GroupMember> findAllByStudyGroup(StudyGroup studyGroup);

	@Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.studyGroup = :studyGroup")
	Integer countMembersByStudyGroup(StudyGroup studyGroup);

	GroupMember findByStudyGroupAndRole(StudyGroup group, RoleOfGroupMember role);

	int countByStudyGroup(StudyGroup studyGroup);

	@Modifying
	@Query("delete from GroupMember gm where gm.studyGroup = :studyGroup")
	void deleteAllByStudyGroup(StudyGroup studyGroup);
}
