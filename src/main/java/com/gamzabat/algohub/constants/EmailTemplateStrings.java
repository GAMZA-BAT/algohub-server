package com.gamzabat.algohub.constants;

public class EmailTemplateStrings {
	public static final String RESET_PASSWORD_SUBJECT = "[AlgoHub] 비밀번호 찾기";
	public static final String RESET_PASSWORD_TITLE = "비밀번호 재설정";
	public static final String RESET_PASSWORD_CONTENT = "버튼을 누르면 새로운 비밀번호 설정을 위한 페이지로 이동합니다. <br/>3시간내 비밀번호를 재설정하지 않으면, 링크는 만료됩니다.";
	public static final String RESET_PASSWORD_BUTTON = "비밀번호 재설정";

	public static final String EMAIL_VALIDATION_SUBJECT = "[AlgoHub] 이메일 유효성 검사";
	public static final String EMAIL_VALIDATION_TITLE = "메일 인증";
	public static final String EMAIL_VALIDATION_CONTENT = "버튼을 누르면 알고헙 계정에 사용할 메일 인증이 완료됩니다. <br/>링크의 제한시간은 3분으로, 이후 링크는 만료됩니다.";
	public static final String EMAIL_VALIDATION_BUTTON = "인증";

	private EmailTemplateStrings() {
		throw new RuntimeException("Can not instantiate : EmailTemplateStrings");
	}

}
