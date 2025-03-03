package com.gamzabat.algohub.auth.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gamzabat.algohub.feature.user.domain.User;

public class CustomUserDetails implements UserDetails {
	private final String identifier;
	private final String password;

	public CustomUserDetails(User user) {
		this.identifier = user.getEmail() != null ? user.getEmail() : user.getNickname();
		this.password = user.getPassword();
	}

	@Override
	public String getUsername() {
		return identifier;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return UserDetails.super.isEnabled();
	}

}
