package com.gamzabat.algohub.feature.recommendation.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_difficulty_monthly_rolling")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDifficultyMonthlyRolling {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "window_start", nullable = false)
	private LocalDateTime windowStart;

	@Column(name = "window_end", nullable = false)
	private LocalDateTime windowEnd;

	@Column(name = "avg_difficulty", nullable = false)
	private Double avgDifficulty;

	@Builder
	public UserDifficultyMonthlyRolling(
		@NotNull Long userId,
		@NotNull LocalDateTime windowStart,
		@NotNull LocalDateTime windowEnd,
		@NotNull Double avgDifficulty) {
		this.userId = userId;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.avgDifficulty = avgDifficulty;
	}
}
