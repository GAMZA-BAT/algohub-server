package com.gamzabat.algohub.feature.notice.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.notice.domain.Notice;

import lombok.Builder;

@Builder
public record GetNoticeResponse(String author,
								String authorImage,
								Long noticeId,
								String content,
								String title,
								String category,
								String createAt,
								boolean isRead) {

	public static GetNoticeResponse toDTO(Notice notice, boolean isRead) {
		return GetNoticeResponse.builder()
			.author(notice.getAuthor().getNickname())
			.authorImage(notice.getAuthor().getProfileImage())
			.noticeId(notice.getId())
			.title(notice.getTitle())
			.content(notice.getContent())
			.category(notice.getCategory())
			.createAt(DateFormatUtil.formatDateTimeForNotice(notice.getCreatedAt()))
			.isRead(isRead)
			.build();
	}
}
