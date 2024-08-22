package com.gamzabat.algohub.feature.board.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
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

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id")
	private StudyGroup studyGroup;

	private String title;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public Board(User user, StudyGroup studyGroup, String title, String content, LocalDateTime createdAt) {
		this.user = user;
		this.title = title;
		this.studyGroup = studyGroup;
		this.content = content;
		this.createdAt = createdAt;
		this.updatedAt = null;
	}

}
