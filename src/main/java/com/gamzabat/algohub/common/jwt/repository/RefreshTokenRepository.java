package com.gamzabat.algohub.common.jwt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.common.jwt.domain.RefreshToken;
import com.gamzabat.algohub.feature.user.domain.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByLoginIdAndUser(String loginId, User user);
}
