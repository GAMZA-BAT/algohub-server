package com.gamzabat.algohub.feature.comment.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@DynamicUpdate
@NoArgsConstructor
@Getter
public abstract class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public void updateComment(String content) {
		this.content = content;
		this.updatedAt = LocalDateTime.now();
	}

	public Comment(User user, String content) {
		this.user = user;
		this.content = content;
		this.createdAt = LocalDateTime.now();
	}
}
