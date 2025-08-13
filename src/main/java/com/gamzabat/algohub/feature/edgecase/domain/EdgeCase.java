package com.gamzabat.algohub.feature.edgecase.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE edge_case SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class EdgeCase {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name="problem_id")
	private Problem problem;

	private String input;
	private String output;

	private Integer like;
	private LocalDateTime deletedAt;
}