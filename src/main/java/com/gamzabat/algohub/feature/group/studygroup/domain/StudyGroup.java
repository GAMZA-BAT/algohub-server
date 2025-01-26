package com.gamzabat.algohub.feature.group.studygroup.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;

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
@SQLDelete(sql = "UPDATE study_group SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@DynamicUpdate
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

	public void editGroupDate(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void editGroupName(String name) {
		this.name = name;
	}

	public void editGroupIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public void editGroupImage(String groupImage) {
		this.groupImage = groupImage;
	}
}
