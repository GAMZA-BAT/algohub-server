package com.gamzabat.algohub.feature.edgecase.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;

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
@SQLDelete(sql = "UPDATE edge_case SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class EdgeCase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer level;
	private String link;
	private Integer problemNumber;
	private String title;
	private String input;
	private String output;
	private Integer like;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User author;
	private LocalDateTime deletedAt;

	@Builder
	public EdgeCase(Integer level, String link, Integer problemNumber, String title, String input, String output, Integer like, User author) {
		this.level = level;
		this.link = link;
		this.problemNumber = problemNumber;
		this.title = title;
		this.input = input;
		this.output = output;
		this.like = like;
		this.author = author;
	}

}