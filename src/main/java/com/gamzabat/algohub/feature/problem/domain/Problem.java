package com.gamzabat.algohub.feature.problem.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE Problem SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Problem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String link;
	private LocalDate startDate;
	private LocalDate endDate;
	private Integer number;
	private String title;
	private Integer level;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id")
	private StudyGroup studyGroup;
	private LocalDateTime deletedAt;

	@Builder
	public Problem(String link, LocalDate startDate, LocalDate endDate, Integer number, String title, Integer level,
		StudyGroup studyGroup) {
		this.link = link;
		this.number = number;
		this.startDate = startDate;
		this.endDate = endDate;
		this.title = title;
		this.level = level;
		this.studyGroup = studyGroup;
	}

	public void editProblemInfo(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
