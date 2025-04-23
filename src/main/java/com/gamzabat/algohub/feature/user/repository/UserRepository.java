package com.gamzabat.algohub.feature.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	@Query("SELECT EXISTS ("
		+ "SELECT 1 FROM User u "
		+ "WHERE u.email = :email "
		+ "AND u.deletedAt IS NULL"
		+ ")")
	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u "
		+ "WHERE u.email = :email "
		+ "AND u.deletedAt IS NULL")
	Optional<User> findByEmail(String email);

	List<User> findAllByBjNickname(String bjNickname);

	boolean existsByNickname(String nickname);

	Optional<User> findByNickname(String nickname);
}
