package com.gamzabat.algohub.feature.board.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.gamzabat.algohub.feature.comment.domain.Comment;
import com.gamzabat.algohub.feature.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
public class BoardComment extends Comment {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@Builder
	public BoardComment(User user, String content,
		LocalDateTime createdAt, Board board) {
		super(user, content, createdAt);
		this.board = board;
	}
}
