package com.gamzabat.algohub.feature.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.notice.domain.Notice;
import com.gamzabat.algohub.feature.notice.domain.NoticeRead;
import com.gamzabat.algohub.feature.user.domain.User;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, Long> {
	boolean existsByNoticeAndUser(Notice notice, User user);
}
