package com.gamzabat.algohub.feature.notification.domain;

import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class NotificationSetting {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private GroupMember member;

	private boolean allNotifications;
	private boolean newProblem;
	private boolean newSolution;
	private boolean newComment;
	private boolean newMember;
	private boolean deadlineReached;

	@Builder
	public NotificationSetting(GroupMember member) {
		this.member = member;
		this.allNotifications = true;
		this.newProblem = true;
		this.newSolution = true;
		this.newComment = true;
		this.newMember = true;
		this.deadlineReached = true;
	}

	public void editSettings(boolean all, boolean newProblem, boolean newSolution, boolean newComment,
		boolean newMember, boolean deadlineReached) {
		this.allNotifications = all;
		this.newProblem = newProblem;
		this.newSolution = newSolution;
		this.newComment = newComment;
		this.newMember = newMember;
		this.deadlineReached = deadlineReached;
	}
}
