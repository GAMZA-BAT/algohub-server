package com.gamzabat.algohub.constants;

public final class ApiConstants {
	public static final String SOLVED_AC_PROBLEM_API_URL = "https://solved.ac/api/v3/problem/lookup?problemIds=";
	public static final String BOJ_USER_PROFILE_URL = "https://www.acmicpc.net/user/";
	public static final String BOJ_PROBLEM_URL = "www.acmicpc.net";

	public static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
	public static final String GITHUB_USER_URL = "https://api.github.com/user";
	public static final String GITHUB_EMAIL_URL = "https://api.github.com/user/emails";

	private ApiConstants() {
		throw new RuntimeException("Can not instantiate : ApiConstants");
	}
}
