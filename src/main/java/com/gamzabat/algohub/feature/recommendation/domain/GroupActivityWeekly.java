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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_activity_weekly")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupActivityWeekly {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id", nullable = false)
	private StudyGroup studyGroup;

	@Column(name = "week_start", nullable = false)
	private LocalDateTime weekStart;

	@Column(name = "week_end", nullable = false)
	private LocalDateTime weekEnd;

	@Column(name = "submissions", nullable = false)
	private Integer submissions;

	@Column(name = "comments", nullable = false)
	private Integer comments;

	@Column(name = "active_score", nullable = false)
	private Double activeScore;
}
