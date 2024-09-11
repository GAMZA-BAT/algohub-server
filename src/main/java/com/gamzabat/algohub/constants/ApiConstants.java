package com.gamzabat.algohub.constants;

public final class ApiConstants {
	public static final String SOLVED_AC_PROBLEM_API_URL = "https://solved.ac/api/v3/problem/lookup?problemIds=";
	public static final String BOJ_USER_PROFILE_URL = "https://www.acmicpc.net/user/";

	private ApiConstants() {
		throw new RuntimeException("Can not instantiate : ApiConstants");
	}
}
