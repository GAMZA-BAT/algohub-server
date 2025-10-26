package com.gamzabat.algohub.feature.solution.domain;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
@Table(
	name = "solution_comment",
	indexes = {
		@Index(name = "idx_comment_solution_created", columnList = "solution_id,created_at")
	}
)
public class SolutionComment extends Comment {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "solution_id")
	private Solution solution;
	private boolean isRead;

	@Builder
	public SolutionComment(Solution solution, User user, String content, boolean isRead) {
		super(user, content);
		this.solution = solution;
		this.isRead = isRead;
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
