package com.gamzabat.algohub.feature.recommendation.domain;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_join_monthly_rolling")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupJoinMonthlyRolling {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id", nullable = false)
	private StudyGroup studyGroup;

	@Column(name = "window_start", nullable = false)
	private LocalDateTime windowStart;

	@Column(name = "window_end", nullable = false)
	private LocalDateTime windowEnd;

	@Column(name = "new_members", nullable = false)
	private Integer newMembers;

	@Column(name = "members_before_window", nullable = false)
	private Integer membersBeforeWindow;

	@Column(name = "join_rate", nullable = false)
	private Double joinRate;

	@Builder
	public GroupJoinMonthlyRolling(
		@NotNull StudyGroup studyGroup,
		@NotNull LocalDateTime windowStart,
		@NotNull LocalDateTime windowEnd,
		@NotNull Integer newMembers,
		@NotNull Integer membersBeforeWindow,
		@NotNull Double joinRate) {
		this.studyGroup = studyGroup;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.newMembers = newMembers;
		this.membersBeforeWindow = membersBeforeWindow;
		this.joinRate = joinRate;
	}
}
