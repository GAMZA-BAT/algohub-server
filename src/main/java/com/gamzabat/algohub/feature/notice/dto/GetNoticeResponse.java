package com.gamzabat.algohub.feature.notice.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.notice.domain.Notice;

import lombok.Builder;

@Builder
public record GetNoticeResponse(String author,
								Long noticeId,
								String content,
								String title,
								String category,
								String createAt,
								boolean isRead) {

	public static GetNoticeResponse toDTO(Notice notice, boolean isRead) {
		return GetNoticeResponse.builder()
			.author(notice.getAuthor().getNickname())
			.noticeId(notice.getId())
			.title(notice.getTitle())
			.content(notice.getContent())
			.category(notice.getCategory())
			.createAt(DateFormatUtil.formatDate(notice.getCreatedAt().toLocalDate()))
			.isRead(isRead)
			.build();
	}
}
