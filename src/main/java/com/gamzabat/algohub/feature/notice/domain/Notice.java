package com.gamzabat.algohub.feature.notice.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Column;
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
public class Notice {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	@Column(columnDefinition = "TEXT")
	private String content;
	private String category;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User author;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id")
	private StudyGroup studyGroup;

	@Builder
	public Notice(User author, StudyGroup studyGroup, String title, String content, String category,
		LocalDateTime createdAt) {
		this.author = author;
		this.title = title;
		this.studyGroup = studyGroup;
		this.content = content;
		this.category = category;
		this.createdAt = createdAt;
	}

	public void updateNotice(String title, String content, String category) {
		this.title = title;
		this.content = content;
		this.category = category;
		this.updatedAt = LocalDateTime.now();
	}

}
