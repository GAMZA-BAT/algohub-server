package com.gamzabat.algohub.feature.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	void deleteByEmail(String email);

	Optional<User> findByBjNickname(String bjNickname);

	Optional<User> findById(Long Id);

	boolean existsByBjNickname(String bjNickname);

	boolean existsByNickname(String nickname);
}
