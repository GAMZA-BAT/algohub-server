package com.gamzabat.algohub.feature.edgecase.domain;

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
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE edge_case_like SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class EdgeCaseLike {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "edge_case_id", nullable = false)
	private EdgeCase edgeCase;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder
	public EdgeCaseLike(EdgeCase edgeCase, User user) {
		this.edgeCase = edgeCase;
		this.user = user;
	}

	void setEdgeCase(EdgeCase edgeCase) {
		this.edgeCase = edgeCase;
	}
}
