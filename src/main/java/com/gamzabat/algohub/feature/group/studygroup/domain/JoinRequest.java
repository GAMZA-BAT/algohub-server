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
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
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

	public JoinRequest(StudyGroup group, User requester) {
		this.group = group;
		this.requester = requester;
	}

	public void updateStatus(JoinRequestStatus status) {
		if (status != JoinRequestStatus.PENDING) {
			return;
		}
		this.status = status;
	}

}


