package com.gamzabat.algohub.feature.group.studygroup.domain;

import java.time.LocalDate;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
public class GroupMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id")
	private StudyGroup studyGroup;
	private LocalDate joinDate;
	@Enumerated(EnumType.STRING)
	private RoleOfGroupMember role;
	@NotNull
	private Boolean isVisible;

	@Builder
	public GroupMember(User user, StudyGroup studyGroup, LocalDate joinDate, RoleOfGroupMember role) {
		this.user = user;
		this.studyGroup = studyGroup;
		this.joinDate = joinDate;
		this.role = role;
		this.isVisible = true;
	}

	public void updateRole(RoleOfGroupMember role) {
		this.role = role;
	}

	public void updateVisibility(boolean isVisible) {
		this.isVisible = isVisible;
	}
}
