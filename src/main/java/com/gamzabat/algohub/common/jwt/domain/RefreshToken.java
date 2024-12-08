package com.gamzabat.algohub.common.jwt.domain;

import java.util.Date;

import org.hibernate.annotations.DynamicUpdate;

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
@DynamicUpdate
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	private String refreshToken;
	private String loginId;
	private Date expirationDateTime;

	@Builder
	public RefreshToken(User user, String refreshToken, Date expirationDateTime, String loginId) {
		this.user = user;
		this.refreshToken = refreshToken;
		this.loginId = loginId;
		this.expirationDateTime = expirationDateTime;
	}

	public void updateRefreshToken(String refreshToken, Date expirationDateTime) {
		this.refreshToken = refreshToken;
		this.expirationDateTime = expirationDateTime;
	}
}
