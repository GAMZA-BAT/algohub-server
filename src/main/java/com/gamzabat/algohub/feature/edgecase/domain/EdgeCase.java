package com.gamzabat.algohub.feature.edgecase.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;

import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
	@Column(columnDefinition = "TEXT")
	private String input;
	@Column(columnDefinition = "TEXT")
	private String output;

	@OneToMany(mappedBy = "edgeCase", fetch = FetchType.LAZY)
	private List<EdgeCaseLike> likes = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User author;

	private LocalDateTime deletedAt;

	@Column(nullable = false)
	private int likeCount = 0;

	@Builder
	public EdgeCase(Integer level, String link, Integer problemNumber, String title, String input, String output, User author) {
		this.level = level;
		this.link = link;
		this.problemNumber = problemNumber;
		this.title = title;
		this.input = input;
		this.output = output;
		this.author = author;
	}

	public void addLike(EdgeCaseLike like) {
		likes.add(like);
		like.setEdgeCase(this);
		likeCount++;
	}
	public void removeLike(EdgeCaseLike like) {
		if (likes.remove(like)) {

			like.setEdgeCase(null);
			if (likeCount > 0)
				likeCount--;
		}
	}



}