package com.gamzabat.algohub.feature.studygroup.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE study_group SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class StudyGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private LocalDate startDate;
	private LocalDate endDate;
	private String introduction;
	private String groupImage;
	private String groupCode;

	private LocalDateTime deletedAt;

	@Builder
	public StudyGroup(String name, LocalDate startDate, LocalDate endDate, String introduction, String groupImage,
		String groupCode) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.introduction = introduction;
		this.groupImage = groupImage;
		this.groupCode = groupCode;
	}

	public void editGroupInfo(String name, LocalDate startDate, LocalDate endDate, String introduction) {
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.introduction = introduction;
	}

	public void editGroupImage(String groupImage) {
		this.groupImage = groupImage;
	}
}
