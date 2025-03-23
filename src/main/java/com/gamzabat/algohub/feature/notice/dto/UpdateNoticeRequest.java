package com.gamzabat.algohub.feature.notice.dto;

public record UpdateNoticeRequest(String title,
								  String content,
								  String category
) {
}
