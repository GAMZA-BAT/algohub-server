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
@Table(name = "group_difficulty_monthly_rolling")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupDifficultyMonthlyRolling {

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

	@Column(name = "avg_difficulty", nullable = false)
	private Double avgDifficulty;

	@Builder
	public GroupDifficultyMonthlyRolling(
		@NotNull StudyGroup studyGroup,
		@NotNull LocalDateTime windowStart,
		@NotNull LocalDateTime windowEnd,
		@NotNull Double avgDifficulty) {
		this.studyGroup = studyGroup;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.avgDifficulty = avgDifficulty;
	}
}
