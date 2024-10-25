package com.gamzabat.algohub.feature.notification.domain;

import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;

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

	private boolean all;
	private boolean newProblem;
	private boolean newSolution;
	private boolean comment;
	private boolean newMember;
	private boolean deadlineReached;

	@Builder
	public NotificationSetting(GroupMember member) {
		this.member = member;
		this.all = true;
		this.newProblem = true;
		this.newSolution = true;
		this.comment = true;
		this.newMember = true;
		this.deadlineReached = true;
	}
}
