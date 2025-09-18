package com.gamzabat.algohub.feature.group.studygroup.domain;

import com.gamzabat.algohub.enums.JoinRequestStatus;
import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class JoinRequest {
	@Id
	@GeneratedValue
	Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private StudyGroup group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User requester;

	@Enumerated(EnumType.STRING)
	private JoinRequestStatus status = JoinRequestStatus.PENDING;

	public void approve() {
		if (status != JoinRequestStatus.PENDING)
			return;
		status = JoinRequestStatus.APPROVE;
	}

	public void reject() {
		if (status != JoinRequestStatus.PENDING)
			return;
		status = JoinRequestStatus.REJECT;
	}

	public void cancel() {
		if (status != JoinRequestStatus.PENDING)
			return;
		status = JoinRequestStatus.CANCEL;
	}
}


