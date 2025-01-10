package com.gamzabat.algohub.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubUserDto {
	private String login;
	private String email;
}
