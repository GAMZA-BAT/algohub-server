package com.gamzabat.algohub.feature.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.user.domain.ResetPassword;

public interface ResetPasswordRepository extends JpaRepository<ResetPassword, Long> {

	@Query("select r from ResetPassword r join fetch r.user where r.token = :token")
	Optional<ResetPassword> findByToken(String token);
}
