package com.gamzabat.algohub.feature.recommendation.domain;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "study_group_tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroupTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id", nullable = false)
	private StudyGroup studyGroup;

	@Enumerated(EnumType.STRING)
	@Column(name = "tag_type", nullable = false, length = 64)
	private TagType tagType;

	@Column(name = "score", nullable = false)
	private Double score;

	@Column(name = "first_achieved_at", nullable = false)
	private LocalDateTime firstAchievedAt;

	@Column(name = "window_start", nullable = false)
	private LocalDateTime windowStart;

	@Column(name = "window_end", nullable = false)
	private LocalDateTime windowEnd;

	@Column(name = "computed_at", nullable = false)
	private LocalDateTime computedAt;

	@Builder
	public StudyGroupTag(
		@NotNull StudyGroup studyGroup,
		@NotNull TagType tagType,
		@NotNull Double score,
		@NotNull LocalDateTime firstAchievedAt,
		@NotNull LocalDateTime windowStart,
		@NotNull LocalDateTime windowEnd,
		@NotNull LocalDateTime computedAt) {
		this.studyGroup = studyGroup;
		this.tagType = tagType;
		this.score = score;
		this.firstAchievedAt = firstAchievedAt;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.computedAt = computedAt;
	}
}
