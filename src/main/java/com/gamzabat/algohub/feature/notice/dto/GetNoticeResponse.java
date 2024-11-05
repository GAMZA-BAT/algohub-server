package com.gamzabat.algohub.feature.notice.dto;

import com.gamzabat.algohub.common.DateFormatUtil;
import com.gamzabat.algohub.feature.notice.domain.Notice;

import lombok.Builder;

@Builder
public record GetNoticeResponse(String author,
								Long noticeId,
								String noticeContent,
								String noticeTitle,
								String createAt) {

	public static GetNoticeResponse toDTO(Notice notice) {
		return GetNoticeResponse.builder()
			.author(notice.getAuthor().getNickname())
			.noticeId(notice.getId())
			.noticeTitle(notice.getTitle())
			.noticeContent(notice.getContent())
			.createAt(DateFormatUtil.formatDate(notice.getCreatedAt().toLocalDate()))
			.build();

	}
}
