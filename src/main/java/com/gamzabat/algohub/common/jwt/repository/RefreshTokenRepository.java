package com.gamzabat.algohub.common.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.common.jwt.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
