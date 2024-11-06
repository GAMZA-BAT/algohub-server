package com.gamzabat.algohub.feature.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.notice.domain.Notice;
import com.gamzabat.algohub.feature.notice.domain.NoticeComment;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {

	List<NoticeComment> findAllByNotice(Notice notice);

	@Modifying
	@Query("delete from NoticeComment c where c.notice = :notice")
	void deleteAllCommentByNotice(Notice notice);
}
