package com.gamzabat.algohub.feature.notice.domain;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
public class NoticeComment extends Comment {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notice_id")
	private Notice notice;

	@Builder
	public NoticeComment(User user, String content, Notice notice) {
		super(user, content);
		this.notice = notice;
	}
}
