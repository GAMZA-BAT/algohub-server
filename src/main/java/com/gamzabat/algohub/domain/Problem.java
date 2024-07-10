package com.gamzabat.algohub.domain;

import java.time.LocalDate;

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
public class Problem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String link;
	private LocalDate deadline;
	private String title;
	private Integer level;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_group_id")
	private StudyGroup studyGroup;

	@Builder
	public Problem(String link, LocalDate deadline, String title, Integer level, StudyGroup studyGroup) {
		this.link = link;
		this.deadline = deadline;
		this.title = title;
		this.level = level;
		this.studyGroup = studyGroup;
	}

	public void editDeadline(LocalDate deadline){this.deadline = deadline;}
}
