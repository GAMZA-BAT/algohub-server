package com.gamzabat.algohub.feature.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
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
public class ResetPassword {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	private String token;

	private LocalDateTime expiredAt;

	private Boolean done = false;

	@Builder
	public ResetPassword(User user, String token) {
		this.user = user;
		this.token = token;
		this.expiredAt = LocalDateTime.now().plusHours(3);
	}

	public void makeDone() {
		this.done = true;
	}
}
