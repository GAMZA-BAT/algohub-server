package com.gamzabat.algohub.feature.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notice.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
	List<Notice> findAllByStudyGroup(StudyGroup studyGroup);

}
