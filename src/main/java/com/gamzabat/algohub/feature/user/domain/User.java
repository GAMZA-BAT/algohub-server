package com.gamzabat.algohub.feature.user.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.gamzabat.algohub.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@SQLDelete(sql = "UPDATE user SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String email;
	private String password;
	private String nickname;
	private String bjNickname;
	private String profileImage;

	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Builder
	public User(String email, String password, String nickname, String bjNickname, String profileImage, Role role) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.bjNickname = bjNickname;
		this.profileImage = profileImage;
		this.role = role;
		this.deletedAt = null;
	}

	public void editNickname(String nickname) {
		this.nickname = nickname;
	}

	public void editBjNickname(String bjNickname) {
		this.bjNickname = bjNickname;
	}

	public void editProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
}
