package com.gamzabat.algohub.feature.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notice.domain.Notice;
import com.gamzabat.algohub.feature.notice.domain.NoticeRead;
import com.gamzabat.algohub.feature.user.domain.User;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, Long> {
	boolean existsByNoticeAndUser(Notice notice, User user);

	@Modifying
	@Query("delete from NoticeRead nr where nr.notice = :notice")
	void deleteAllByNotice(Notice notice);

	@Modifying
	@Query("delete from NoticeRead nr where nr.notice.studyGroup = :group")
	void deleteAllByStudyGroup(StudyGroup group);

	@Modifying
	@Query("delete from NoticeRead nr where nr.notice.studyGroup = :group and nr.user = :user")
	void deleteAllByStudyGroupAndUser(StudyGroup group, User user);
}
