package com.gamzabat.algohub.feature.studygroup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.studygroup.domain.BookmarkedStudyGroup;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;

public interface BookmarkedStudyGroupRepository extends JpaRepository<BookmarkedStudyGroup, Long> {
	Optional<BookmarkedStudyGroup> findByUserAndStudyGroup(User user, StudyGroup studyGroup);

	List<BookmarkedStudyGroup> findAllByUser(User user);
}