package com.gamzabat.algohub.feature.notification.domain;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.problem.domain.Problem;
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

@Entity
@Getter
@NoArgsConstructor
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id")
	private StudyGroup studyGroup;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "problem_id")
	private Problem problem;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "solution_id")
	private Solution solution;
	private String message;
	private boolean isRead;
	private String subContent;
	private LocalDateTime createdAt;

	@Builder
	public Notification(User user, StudyGroup studyGroup, Problem problem, Solution solution, String message,
		boolean isRead, String subContent) {
		this.user = user;
		this.studyGroup = studyGroup;
		this.problem = problem;
		this.solution = solution;
		this.message = message;
		this.isRead = isRead;
		this.subContent = subContent;
		this.createdAt = LocalDateTime.now();
	}

	public void updateIsRead() {
		this.isRead = true;
	}
}
