package com.gamzabat.algohub.feature.comment.domain;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.solution.domain.Solution;
import com.gamzabat.algohub.feature.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "solution_id")
	private Solution solution;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private String content;
	private LocalDateTime createdAt;

	@Builder
	public Comment(Solution solution, User user, String content, LocalDateTime createdAt) {
		this.solution = solution;
		this.user = user;
		this.content = content;
		this.createdAt = createdAt;
	}

	public void upadateComment(String content,LocalDateTime createdAt) {
		this.content = content;
		this.createdAt = LocalDateTime.now();
	}

}
