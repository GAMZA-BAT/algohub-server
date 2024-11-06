package com.gamzabat.algohub.feature.notification.enums;

public enum NotificationCategory {
	PROBLEM_STARTED("[%s] 문제가 시작되었습니다! 지금 도전해보세요!"),
	NEW_SOLUTION_POSTED("%s님이 새로운 풀이를 등록했습니다! 풀이를 확인하고 의견을 나눠보세요."),
	NEW_MEMBER_JOINED("%s님이 스터디에 합류했습니다!"),
	NEW_COMMENT_POSTED("%s님이 내 풀이에 코멘트를 남겼습니다! 어떤 리뷰인지 확인해보세요."),
	PROBLEM_DEADLINE_REACHED("[%s] 문제의 마감이 오늘입니다! 아직 해결하지 못했다면 지금 도전해보세요!");

	private final String message;

	NotificationCategory(String message) {
		this.message = message;
	}

	public String getMessage(Object... args) {
		return String.format(message, args);
	}
}
