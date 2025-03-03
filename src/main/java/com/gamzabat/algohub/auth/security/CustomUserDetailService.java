package com.gamzabat.algohub.auth.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundUserException;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

public class CustomUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(identifier)
			.or(() -> userRepository.findByNickname(identifier))
			.orElseThrow(() -> new CannotFoundUserException(HttpStatus.NOT_FOUND.value(), "해당 유저는 존재하지 않습니다."));

		return new CustomUserDetails(user);
	}
}
