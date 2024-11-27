package com.gamzabat.algohub.feature.solution.domain;

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
public class SolutionComment extends Comment {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "solution_id")
	private Solution solution;

	@Builder
	public SolutionComment(Solution solution, User user, String content) {
		super(user, content);
		this.solution = solution;
	}

}
