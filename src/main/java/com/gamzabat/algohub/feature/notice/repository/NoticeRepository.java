package com.gamzabat.algohub.feature.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notice.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
	Page<Notice> findAllByStudyGroup(StudyGroup studyGroup, Pageable pageable);

	@Modifying
	@Query("delete from Notice n where n.studyGroup = :studyGroup")
	void deleteAllByStudyGroup(StudyGroup studyGroup);
}
