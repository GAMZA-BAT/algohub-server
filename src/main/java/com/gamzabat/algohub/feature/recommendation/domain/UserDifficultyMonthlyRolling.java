package com.gamzabat.algohub.feature.recommendation.domain;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.user.domain.User;

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
@Table(name = "user_difficulty_monthly_rolling")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDifficultyMonthlyRolling {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "window_start", nullable = false)
	private LocalDateTime windowStart;

	@Column(name = "window_end", nullable = false)
	private LocalDateTime windowEnd;

	@Column(name = "avg_difficulty", nullable = false)
	private Double avgDifficulty;

	@Builder
	public UserDifficultyMonthlyRolling(
		@NotNull User user,
		@NotNull LocalDateTime windowStart,
		@NotNull LocalDateTime windowEnd,
		@NotNull Double avgDifficulty) {
		this.user = user;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.avgDifficulty = avgDifficulty;
	}
}
